package com.nahunp.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nahunp.todoapp.data.local.entity.ServerKeyRow
import com.nahunp.todoapp.data.local.entity.TodoListEntity
import com.nahunp.todoapp.data.local.entity.TodoListWithItemsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {

    @Transaction
    @Query("SELECT * FROM todo_lists ORDER BY name")
    fun observeAllWithItems(): Flow<List<TodoListWithItemsEntity>>

    @Transaction
    @Query("SELECT * FROM todo_lists WHERE localKey = :localKey")
    fun observeWithItems(localKey: Int): Flow<TodoListWithItemsEntity?>

    // Returns the Room-assigned localKey — the id a newly-created (still
    // offline) list is known by everywhere above data/local until/unless
    // it syncs, and forever after that too (see TodoListEntity's doc
    // comment).
    @Insert
    suspend fun insert(list: TodoListEntity): Long

    @Query("UPDATE todo_lists SET name = :name WHERE localKey = :localKey")
    suspend fun updateName(localKey: Int, name: String)

    @Query("UPDATE todo_lists SET serverId = :serverId WHERE localKey = :localKey")
    suspend fun setServerId(localKey: Int, serverId: Int)

    @Query("SELECT serverId FROM todo_lists WHERE localKey = :localKey")
    suspend fun getServerId(localKey: Int): Int?

    @Query("DELETE FROM todo_lists WHERE localKey = :localKey")
    suspend fun delete(localKey: Int)

    @Query("DELETE FROM todo_lists")
    suspend fun clearAll()

    // The merge side of a sync pull (LocalTodoDataSource.mergeServerState):
    // which already-synced local rows match which server rows, so an
    // update can reuse the existing localKey instead of every pull
    // reassigning fresh ones (which would break any screen currently
    // observing a list via its old key).
    @Query("SELECT serverId, localKey FROM todo_lists WHERE serverId IS NOT NULL")
    suspend fun getSyncedKeyMap(): List<ServerKeyRow>

    // A synced list absent from the latest pull was deleted server-side
    // (by this device or another client) — drop it locally too. Never
    // touches a row with serverId IS NULL (an offline-created list still
    // mid-sync isn't part of this pull's result set at all).
    @Query("DELETE FROM todo_lists WHERE serverId IS NOT NULL AND serverId NOT IN (:keepServerIds)")
    suspend fun deleteSyncedNotIn(keepServerIds: List<Int>)
}
