package com.nahunp.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The offline write queue — every mutation made through TodoListRepository
 * writes to the local cache immediately (optimistic) AND inserts one of
 * these, in the same order the user made the change. SyncManager drains
 * this table strictly FIFO (`id ASC`, autoincrement) once a connection is
 * available, since order matters here — e.g. a list must actually exist on
 * the server (its CREATE_LIST processed) before an item can be added to it.
 *
 * `listLocalKey`/`itemLocalKey` are the entities' *stable* local keys
 * (TodoListEntity.localKey / TodoItemEntity.localKey) — never rewritten,
 * regardless of sync state. SyncManager resolves the real server id to
 * call the API with by looking it up via this key at push time (see
 * SyncManager.requireServerListId/requireServerIds), which works cleanly
 * because FIFO order guarantees a CREATE op has already run and set that
 * server id before any later op referencing the same key is processed.
 *
 * DELETE_LIST/DELETE_ITEM are the one exception: TodoListRepositoryImpl
 * deletes the local row immediately (optimistic), so there's no row left
 * to look up a server id from by push time — its `payload` holds the
 * server id captured *at enqueue time* instead (stringified int), and is
 * only enqueued at all if the row had one (nothing to delete remotely for
 * a list/item that was created offline and never synced).
 *
 * For every other op type, `payload` holds whatever single "new value" it
 * needs (a name, a title, an enum name, an ISO due-date string). SET_DUE_DATE
 * is the one case that needs to represent "clear the due date" (a real
 * `null` to send) as distinct from "no payload at all" — it uses `""` as
 * that sentinel (see TodoListRepositoryImpl.setTodoItemDueDate and
 * SyncManager.applyOperation), since a real ISO-8601 instant string is
 * never empty.
 */
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val listLocalKey: Int,
    val itemLocalKey: Int? = null,
    val payload: String? = null,
)

enum class PendingOperationType {
    CREATE_LIST,
    RENAME_LIST,
    DELETE_LIST,
    CREATE_ITEM,
    RENAME_ITEM,
    SET_PRIORITY,
    SET_CATEGORY,
    SET_DUE_DATE,
    COMPLETE_ITEM,
    REOPEN_ITEM,
    DELETE_ITEM,
}
