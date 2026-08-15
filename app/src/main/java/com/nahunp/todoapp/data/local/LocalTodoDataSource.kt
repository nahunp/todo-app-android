package com.nahunp.todoapp.data.local

import androidx.room.withTransaction
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.dao.TodoItemDao
import com.nahunp.todoapp.data.local.dao.TodoListDao
import com.nahunp.todoapp.data.local.entity.TodoItemEntity
import com.nahunp.todoapp.data.local.entity.TodoListEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The handful of local-cache operations that touch more than one table (or
 * need a lookup-before-write) and so don't belong on a single DAO —
 * everything single-table/single-query goes straight through the relevant
 * DAO instead (TodoListRepositoryImpl, AuthRepositoryImpl).
 */
@Singleton
class LocalTodoDataSource @Inject constructor(
    private val database: TodoDatabase,
    private val listDao: TodoListDao,
    private val itemDao: TodoItemDao,
    private val pendingOperationDao: PendingOperationDao,
) {
    // Sync-internal server-id bookkeeping — SyncManager only. See
    // TodoListEntity's doc comment on why localKey/serverId are separate.
    suspend fun getListServerId(localKey: Int): Int? = listDao.getServerId(localKey)
    suspend fun setListServerId(localKey: Int, serverId: Int) = listDao.setServerId(localKey, serverId)
    suspend fun getItemServerId(localKey: Int): Int? = itemDao.getServerId(localKey)
    suspend fun setItemServerId(localKey: Int, serverId: Int) = itemDao.setServerId(localKey, serverId)

    // The full-pull side of sync (SyncManager.pullAll) — merges server
    // truth into the cache rather than a blind clear+reinsert, so a list
    // that already existed locally keeps its localKey (and therefore
    // doesn't orphan a detail screen that's observing it via that key
    // mid-pull — see TodoListEntity's doc comment for the bug this avoids).
    // Wrapped in one transaction so Room's Flow observers see one atomic
    // update, not a flicker of intermediate states.
    suspend fun mergeServerState(lists: List<ServerListSnapshot>) {
        database.withTransaction {
            val existingByServerId = listDao.getSyncedKeyMap().associateBy { it.serverId }
            listDao.deleteSyncedNotIn(lists.map { it.serverId })

            for (serverList in lists) {
                val existingLocalKey = existingByServerId[serverList.serverId]?.localKey
                val localKey = if (existingLocalKey != null) {
                    listDao.updateName(existingLocalKey, serverList.name)
                    existingLocalKey
                } else {
                    listDao.insert(TodoListEntity(serverId = serverList.serverId, name = serverList.name)).toInt()
                }
                mergeItems(localKey, serverList.items)
            }
        }
    }

    private suspend fun mergeItems(listLocalKey: Int, items: List<ServerItemSnapshot>) {
        val existingByServerId = itemDao.getSyncedKeyMapForList(listLocalKey).associateBy { it.serverId }
        itemDao.deleteSyncedNotIn(listLocalKey, items.map { it.serverId })

        for (serverItem in items) {
            val existingLocalKey = existingByServerId[serverItem.serverId]?.localKey
            if (existingLocalKey != null) {
                itemDao.updateFromServer(
                    localKey = existingLocalKey,
                    title = serverItem.title,
                    notes = serverItem.notes,
                    isDone = serverItem.isDone,
                    completedAt = serverItem.completedAt,
                    priority = serverItem.priority,
                    dueDate = serverItem.dueDate,
                    dueDateState = serverItem.dueDateState,
                    category = serverItem.category,
                )
            } else {
                itemDao.insert(
                    TodoItemEntity(
                        listLocalKey = listLocalKey,
                        serverId = serverItem.serverId,
                        title = serverItem.title,
                        notes = serverItem.notes,
                        isDone = serverItem.isDone,
                        completedAt = serverItem.completedAt,
                        priority = serverItem.priority,
                        dueDate = serverItem.dueDate,
                        dueDateState = serverItem.dueDateState,
                        category = serverItem.category,
                    ),
                )
            }
        }
    }

    // Logout (or account deletion, which always logs out right after —
    // see TodoListViewModel.deleteAccount) — wipe the cache and the queue
    // so a different account signing in on the same device never sees a
    // stray previous account's lists, and nothing tries to push a dead
    // account's queued changes later.
    suspend fun clearAll() {
        database.withTransaction {
            listDao.clearAll()
            itemDao.clearAll()
            pendingOperationDao.clearAll()
        }
    }
}
