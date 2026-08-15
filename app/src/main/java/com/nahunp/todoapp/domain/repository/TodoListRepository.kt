package com.nahunp.todoapp.domain.repository

import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.model.SyncStatus
import com.nahunp.todoapp.domain.model.TodoList
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Local-first: reads come from the on-device cache via Flow, always
 * available offline; writes apply to that cache immediately (optimistic)
 * and queue for the backend, pushed automatically once a connection comes
 * back -- "remote wins" resolves any conflict once that sync completes.
 * See CLAUDE.md's "Offline support" section and data/sync/SyncManager.kt
 * for the actual mechanism; TodoListRepositoryImpl is just the local-cache
 * read/write side of it.
 *
 * Mutations no longer throw ApiException synchronously the way they used
 * to when this was a thin network wrapper -- a local write always
 * succeeds. Any error from the network side (a duplicate name the backend
 * rejects, a 404 because something was deleted elsewhere, no connection at
 * all) surfaces later, asynchronously, via [syncStatus].lastError, not as
 * an exception the caller of e.g. createTodoList can catch. This is a
 * deliberate trade-off of going offline-first, not an oversight -- see
 * CLAUDE.md.
 *
 * The one thing this repository deliberately does NOT cover: account
 * creation. AuthRepository.register still requires a live connection
 * outright, same as it always has -- nothing here queues an offline
 * registration.
 */
interface TodoListRepository {
    fun observeTodoLists(): Flow<List<TodoList>>
    fun observeTodoList(id: Int): Flow<TodoList?>
    val syncStatus: StateFlow<SyncStatus>

    suspend fun createTodoList(name: String)
    suspend fun renameTodoList(id: Int, newName: String)
    suspend fun deleteTodoList(id: Int)
    suspend fun addTodoItem(listId: Int, title: String)
    suspend fun renameTodoItem(listId: Int, itemId: Int, newTitle: String)
    suspend fun setTodoItemPriority(listId: Int, itemId: Int, priority: Priority)
    suspend fun setTodoItemCategory(listId: Int, itemId: Int, category: Category)
    suspend fun setTodoItemDueDate(listId: Int, itemId: Int, dueDate: Instant?)
    suspend fun completeTodoItem(listId: Int, itemId: Int)
    suspend fun reopenTodoItem(listId: Int, itemId: Int)
    suspend fun removeTodoItem(listId: Int, itemId: Int)
}
