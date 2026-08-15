package com.nahunp.todoapp.data.repository

import com.nahunp.todoapp.data.local.LocalTodoDataSource
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.dao.TodoItemDao
import com.nahunp.todoapp.data.local.dao.TodoListDao
import com.nahunp.todoapp.data.local.entity.PendingOperationEntity
import com.nahunp.todoapp.data.local.entity.PendingOperationType
import com.nahunp.todoapp.data.local.entity.TodoItemEntity
import com.nahunp.todoapp.data.local.entity.TodoListEntity
import com.nahunp.todoapp.data.local.toDomain
import com.nahunp.todoapp.data.sync.SyncManager
import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.DueDateState
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.model.SyncStatus
import com.nahunp.todoapp.domain.model.TodoList
import com.nahunp.todoapp.domain.repository.TodoListRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * `id`/`listId`/`itemId` throughout this class (and TodoListRepository's
 * interface) mean the local cache's stable key (TodoListEntity.localKey /
 * TodoItemEntity.localKey) — that's what the domain layer and every
 * ViewModel/screen above it actually deal in. The server's own id is a
 * sync-internal concern this class never touches directly; LocalTodoDataSource
 * and SyncManager own that translation. See TodoListEntity's doc comment
 * for why the two are kept deliberately separate.
 */
@Singleton
class TodoListRepositoryImpl @Inject constructor(
    private val listDao: TodoListDao,
    private val itemDao: TodoItemDao,
    private val pendingOperationDao: PendingOperationDao,
    private val local: LocalTodoDataSource,
    private val syncManager: SyncManager,
) : TodoListRepository {

    override fun observeTodoLists(): Flow<List<TodoList>> =
        listDao.observeAllWithItems().map { rows -> rows.map { it.toDomain() } }

    override fun observeTodoList(id: Int): Flow<TodoList?> =
        listDao.observeWithItems(id).map { it?.toDomain() }

    override val syncStatus: StateFlow<SyncStatus> = syncManager.status

    override suspend fun createTodoList(name: String) {
        val localKey = listDao.insert(TodoListEntity(name = name)).toInt()
        enqueue(PendingOperationType.CREATE_LIST, listLocalKey = localKey, payload = name)
    }

    override suspend fun renameTodoList(id: Int, newName: String) {
        listDao.updateName(id, newName)
        enqueue(PendingOperationType.RENAME_LIST, listLocalKey = id, payload = newName)
    }

    override suspend fun deleteTodoList(id: Int) {
        // Read before delete -- once the row's gone there's no way to ask
        // "did this ever sync" again, and DELETE_LIST needs that answer
        // (and the server id itself) captured now. See
        // PendingOperationEntity's doc comment.
        val serverId = local.getListServerId(id)
        listDao.delete(id) // cascades to this list's items locally too
        pendingOperationDao.deleteForList(id) // any still-queued edits for it are moot now
        if (serverId != null) {
            enqueue(PendingOperationType.DELETE_LIST, listLocalKey = id, payload = serverId.toString())
        }
    }

    override suspend fun addTodoItem(listId: Int, title: String) {
        val localKey = itemDao.insert(
            TodoItemEntity(
                listLocalKey = listId,
                title = title,
                notes = null,
                isDone = false,
                completedAt = null,
                priority = Priority.Medium.name,
                dueDate = null,
                dueDateState = DueDateState.None.name,
                category = Category.None.name,
            ),
        ).toInt()
        enqueue(PendingOperationType.CREATE_ITEM, listLocalKey = listId, itemLocalKey = localKey, payload = title)
    }

    override suspend fun renameTodoItem(listId: Int, itemId: Int, newTitle: String) {
        itemDao.rename(itemId, newTitle)
        enqueue(PendingOperationType.RENAME_ITEM, listLocalKey = listId, itemLocalKey = itemId, payload = newTitle)
    }

    override suspend fun setTodoItemPriority(listId: Int, itemId: Int, priority: Priority) {
        itemDao.setPriority(itemId, priority.name)
        enqueue(PendingOperationType.SET_PRIORITY, listLocalKey = listId, itemLocalKey = itemId, payload = priority.name)
    }

    override suspend fun setTodoItemCategory(listId: Int, itemId: Int, category: Category) {
        itemDao.setCategory(itemId, category.name)
        enqueue(PendingOperationType.SET_CATEGORY, listLocalKey = listId, itemLocalKey = itemId, payload = category.name)
    }

    override suspend fun setTodoItemDueDate(listId: Int, itemId: Int, dueDate: Instant?) {
        itemDao.setDueDate(itemId, dueDate?.toString(), computeDueDateStateLocally(dueDate).name)
        // "" sentinel for "clear it" -- see PendingOperationEntity's doc
        // comment; a real ISO instant string is never empty.
        enqueue(PendingOperationType.SET_DUE_DATE, listLocalKey = listId, itemLocalKey = itemId, payload = dueDate?.toString() ?: "")
    }

    override suspend fun completeTodoItem(listId: Int, itemId: Int) {
        itemDao.setDone(itemId, isDone = true, completedAt = Instant.now().toString())
        enqueue(PendingOperationType.COMPLETE_ITEM, listLocalKey = listId, itemLocalKey = itemId, payload = null)
    }

    override suspend fun reopenTodoItem(listId: Int, itemId: Int) {
        itemDao.setDone(itemId, isDone = false, completedAt = null)
        enqueue(PendingOperationType.REOPEN_ITEM, listLocalKey = listId, itemLocalKey = itemId, payload = null)
    }

    override suspend fun removeTodoItem(listId: Int, itemId: Int) {
        val serverId = local.getItemServerId(itemId)
        itemDao.delete(itemId)
        pendingOperationDao.deleteForItem(itemId)
        if (serverId != null) {
            enqueue(PendingOperationType.DELETE_ITEM, listLocalKey = listId, itemLocalKey = itemId, payload = serverId.toString())
        }
    }

    private suspend fun enqueue(type: PendingOperationType, listLocalKey: Int, itemLocalKey: Int? = null, payload: String?) {
        pendingOperationDao.enqueue(
            PendingOperationEntity(type = type.name, listLocalKey = listLocalKey, itemLocalKey = itemLocalKey, payload = payload),
        )
        syncManager.requestSync()
    }
}

// Server-computed normally (see domain/model/TodoItem.kt's doc comment on
// DueDateState) -- this is a best-effort local approximation only, purely
// for optimistic offline display, using the device's own clock/timezone
// rather than the server's. Corrected for real on the next successful sync
// pull. See CLAUDE.md's "Offline support" section.
private fun computeDueDateStateLocally(dueDate: Instant?): DueDateState {
    if (dueDate == null) return DueDateState.None
    val today = LocalDate.now()
    val dueDay = dueDate.atZone(ZoneId.systemDefault()).toLocalDate()
    return when {
        dueDay.isBefore(today) -> DueDateState.Overdue
        dueDay.isEqual(today) -> DueDateState.Today
        else -> DueDateState.Upcoming
    }
}
