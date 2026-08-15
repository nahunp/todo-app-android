package com.nahunp.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nahunp.todoapp.data.local.entity.ServerKeyRow
import com.nahunp.todoapp.data.local.entity.TodoItemEntity

@Dao
interface TodoItemDao {

    @Insert
    suspend fun insert(item: TodoItemEntity): Long

    @Query("DELETE FROM todo_items WHERE localKey = :localKey")
    suspend fun delete(localKey: Int)

    @Query("DELETE FROM todo_items")
    suspend fun clearAll()

    @Query("UPDATE todo_items SET title = :title WHERE localKey = :localKey")
    suspend fun rename(localKey: Int, title: String)

    @Query("UPDATE todo_items SET priority = :priority WHERE localKey = :localKey")
    suspend fun setPriority(localKey: Int, priority: String)

    @Query("UPDATE todo_items SET category = :category WHERE localKey = :localKey")
    suspend fun setCategory(localKey: Int, category: String)

    @Query("UPDATE todo_items SET dueDate = :dueDate, dueDateState = :dueDateState WHERE localKey = :localKey")
    suspend fun setDueDate(localKey: Int, dueDate: String?, dueDateState: String)

    @Query("UPDATE todo_items SET isDone = :isDone, completedAt = :completedAt WHERE localKey = :localKey")
    suspend fun setDone(localKey: Int, isDone: Boolean, completedAt: String?)

    @Query("UPDATE todo_items SET serverId = :serverId WHERE localKey = :localKey")
    suspend fun setServerId(localKey: Int, serverId: Int)

    @Query("SELECT serverId FROM todo_items WHERE localKey = :localKey")
    suspend fun getServerId(localKey: Int): Int?

    // See TodoListDao's matching pair for what these are for.
    @Query("SELECT serverId, localKey FROM todo_items WHERE listLocalKey = :listLocalKey AND serverId IS NOT NULL")
    suspend fun getSyncedKeyMapForList(listLocalKey: Int): List<ServerKeyRow>

    @Query("DELETE FROM todo_items WHERE listLocalKey = :listLocalKey AND serverId IS NOT NULL AND serverId NOT IN (:keepServerIds)")
    suspend fun deleteSyncedNotIn(listLocalKey: Int, keepServerIds: List<Int>)

    @Query(
        """
        UPDATE todo_items SET
            title = :title, notes = :notes, isDone = :isDone, completedAt = :completedAt,
            priority = :priority, dueDate = :dueDate, dueDateState = :dueDateState, category = :category
        WHERE localKey = :localKey
        """,
    )
    suspend fun updateFromServer(
        localKey: Int,
        title: String,
        notes: String?,
        isDone: Boolean,
        completedAt: String?,
        priority: String,
        dueDate: String?,
        dueDateState: String,
        category: String,
    )
}
