package com.nahunp.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nahunp.todoapp.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Insert
    suspend fun enqueue(op: PendingOperationEntity): Long

    // FIFO pop, one at a time — see PendingOperationEntity's doc comment on
    // why strict order matters (a list must exist on the server before an
    // item can be added to it, etc).
    @Query("SELECT * FROM pending_operations ORDER BY id LIMIT 1")
    suspend fun peekFirst(): PendingOperationEntity?

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun delete(id: Long)

    // Called when a list/item is deleted locally before ever finishing
    // sync — any still-queued edits for it are moot. Safe to call
    // unconditionally (whether or not it ever reached the server) since
    // this is keyed on the stable local key, not a server id.
    @Query("DELETE FROM pending_operations WHERE listLocalKey = :listLocalKey")
    suspend fun deleteForList(listLocalKey: Int)

    @Query("DELETE FROM pending_operations WHERE itemLocalKey = :itemLocalKey")
    suspend fun deleteForItem(itemLocalKey: Int)

    @Query("DELETE FROM pending_operations")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>
}
