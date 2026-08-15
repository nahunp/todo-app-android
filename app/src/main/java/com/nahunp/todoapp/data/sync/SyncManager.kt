package com.nahunp.todoapp.data.sync

import com.nahunp.todoapp.core.connectivity.ConnectivityObserver
import com.nahunp.todoapp.core.network.TodoApiService
import com.nahunp.todoapp.data.local.LocalTodoDataSource
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.entity.PendingOperationEntity
import com.nahunp.todoapp.data.local.entity.PendingOperationType
import com.nahunp.todoapp.data.local.toSnapshot
import com.nahunp.todoapp.data.remote.dto.CreateTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.CreateTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.SetCategoryRequestDto
import com.nahunp.todoapp.data.remote.dto.SetDueDateRequestDto
import com.nahunp.todoapp.data.remote.dto.SetPriorityRequestDto
import com.nahunp.todoapp.di.ApplicationScope
import com.nahunp.todoapp.domain.model.SyncStatus
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

/**
 * The offline-sync engine — see CLAUDE.md's "Offline support" section for
 * the design in full. In short: TodoListRepositoryImpl writes to the local
 * cache immediately and enqueues a PendingOperationEntity for every
 * mutation; this class drains that queue against the real backend
 * whenever there's a connection, resolving each op's stable local key to
 * whatever the current real server id is (see requireServerListId/
 * requireServerIds), then does one full merge pull that reconciles the
 * local cache with server truth ("remote wins" — see pullAll's comment on
 * what that means for a queued op that failed).
 *
 * requestSync() is safe to call liberally (every mutation does) — actual
 * work is serialized behind syncMutex, so overlapping calls just await the
 * in-flight run rather than double-processing the queue.
 */
@Singleton
class SyncManager @Inject constructor(
    private val api: TodoApiService,
    private val local: LocalTodoDataSource,
    private val pendingOperationDao: PendingOperationDao,
    private val connectivityObserver: ConnectivityObserver,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val syncMutex = Mutex()

    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    init {
        scope.launch {
            pendingOperationDao.observeCount().collectLatest { count ->
                _status.update { it.copy(pendingCount = count) }
            }
        }
        scope.launch {
            connectivityObserver.isOnline.collectLatest { online ->
                _status.update { it.copy(isOnline = online) }
                if (online) requestSync()
            }
        }
    }

    fun requestSync() {
        scope.launch { syncNow() }
    }

    private suspend fun syncNow() {
        // A no-op if a run is already in flight -- that run will see
        // whatever was just enqueued anyway (peekFirst re-queries), so
        // there's nothing this call needs to wait for or repeat.
        if (syncMutex.isLocked) return
        syncMutex.withLock {
            if (!connectivityObserver.isOnline.first()) return@withLock
            _status.update { it.copy(isSyncing = true) }
            val error = try {
                pushQueue()
                pullAll()
                null
            } catch (e: IOException) {
                "Couldn't reach the server. Check your connection and try again."
            } catch (e: HttpException) {
                "Sync failed (${e.code()}). Please try again."
            }
            _status.update { it.copy(isSyncing = false, lastError = error) }
        }
    }

    // Strictly FIFO, one op at a time, re-reading from the DB each
    // iteration -- a server id resolved mid-loop (e.g. a CREATE_LIST just
    // below it in the queue) must be visible by the time a later op
    // referencing the same key is read.
    private suspend fun pushQueue() {
        while (true) {
            if (!connectivityObserver.isOnline.first()) return
            val op = pendingOperationDao.peekFirst() ?: return
            try {
                applyOperation(op)
                pendingOperationDao.delete(op.id)
            } catch (e: HttpException) {
                // The server rejected this specific op outright (404 --
                // the target was deleted server-side or by another
                // client already; 400 -- a stale request against
                // whatever the server's current state is). Retrying the
                // exact same op won't help, and per "remote wins" the
                // pull that follows this loop is what actually resolves
                // the disagreement, not this abandoned local intent. Drop
                // it and move on to the rest of the queue.
                pendingOperationDao.delete(op.id)
            }
            // IOException (genuine network failure mid-push) intentionally
            // propagates out of pushQueue to syncNow's catch -- op stays
            // queued, retried on the next successful connectivity event.
        }
    }

    private suspend fun applyOperation(op: PendingOperationEntity) {
        when (PendingOperationType.valueOf(op.type)) {
            PendingOperationType.CREATE_LIST -> {
                val response = api.createTodoList(CreateTodoListRequestDto(requireNotNull(op.payload)))
                local.setListServerId(op.listLocalKey, response.id)
            }
            PendingOperationType.RENAME_LIST -> {
                val listServerId = requireServerListId(op.listLocalKey)
                api.renameTodoList(listServerId, RenameTodoListRequestDto(requireNotNull(op.payload)))
            }
            PendingOperationType.DELETE_LIST -> {
                // Captured at enqueue time, not looked up here -- the
                // local row is already gone by the time this runs. See
                // PendingOperationEntity's doc comment.
                api.deleteTodoList(requireNotNull(op.payload).toInt())
            }
            PendingOperationType.CREATE_ITEM -> {
                val listServerId = requireServerListId(op.listLocalKey)
                val response = api.addTodoItem(listServerId, CreateTodoItemRequestDto(requireNotNull(op.payload)))
                local.setItemServerId(requireNotNull(op.itemLocalKey), response.id)
            }
            PendingOperationType.RENAME_ITEM -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                api.renameTodoItem(listServerId, itemServerId, RenameTodoItemRequestDto(requireNotNull(op.payload)))
            }
            PendingOperationType.SET_PRIORITY -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                api.setTodoItemPriority(listServerId, itemServerId, SetPriorityRequestDto(requireNotNull(op.payload)))
            }
            PendingOperationType.SET_CATEGORY -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                api.setTodoItemCategory(listServerId, itemServerId, SetCategoryRequestDto(requireNotNull(op.payload)))
            }
            PendingOperationType.SET_DUE_DATE -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                // "" is the enqueue-time sentinel for "clear it" -- see
                // PendingOperationEntity's doc comment.
                val dueDate = op.payload?.takeIf { it.isNotEmpty() }
                api.setTodoItemDueDate(listServerId, itemServerId, SetDueDateRequestDto(dueDate))
            }
            PendingOperationType.COMPLETE_ITEM -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                api.completeTodoItem(listServerId, itemServerId)
            }
            PendingOperationType.REOPEN_ITEM -> {
                val (listServerId, itemServerId) = requireServerIds(op)
                api.reopenTodoItem(listServerId, itemServerId)
            }
            PendingOperationType.DELETE_ITEM -> {
                val listServerId = requireServerListId(op.listLocalKey)
                // Item's server id captured at enqueue time -- same
                // reasoning as DELETE_LIST above.
                api.removeTodoItem(listServerId, requireNotNull(op.payload).toInt())
            }
        }
    }

    private suspend fun requireServerListId(listLocalKey: Int): Int =
        requireNotNull(local.getListServerId(listLocalKey)) {
            "List $listLocalKey has no server id yet -- its CREATE_LIST should have run first (queue order violated)"
        }

    private suspend fun requireServerIds(op: PendingOperationEntity): Pair<Int, Int> {
        val listServerId = requireServerListId(op.listLocalKey)
        val itemServerId = requireNotNull(local.getItemServerId(requireNotNull(op.itemLocalKey))) {
            "Item ${op.itemLocalKey} has no server id yet -- its CREATE_ITEM should have run first (queue order violated)"
        }
        return listServerId to itemServerId
    }

    // The "remote wins" half of the contract: once the queue above is
    // drained (successfully pushed, or dropped as unfixable), whatever the
    // server now says is simply the truth -- merge it into the local
    // cache rather than trying to reconcile field by field. Fetches each
    // list's full detail (the summary endpoint doesn't include items --
    // see GetTodoListsQuery.cs in the web repo) since offline mode needs
    // every item cached, not just the ones a detail screen happened to
    // have been opened for.
    private suspend fun pullAll() {
        val summaries = api.getTodoLists()
        val fullLists = summaries.map { api.getTodoList(it.id) }
        local.mergeServerState(fullLists.map { it.toSnapshot() })
    }
}
