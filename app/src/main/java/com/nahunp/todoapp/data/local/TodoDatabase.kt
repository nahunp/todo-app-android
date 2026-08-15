package com.nahunp.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nahunp.todoapp.data.local.dao.PendingOperationDao
import com.nahunp.todoapp.data.local.dao.TodoItemDao
import com.nahunp.todoapp.data.local.dao.TodoListDao
import com.nahunp.todoapp.data.local.entity.PendingOperationEntity
import com.nahunp.todoapp.data.local.entity.TodoItemEntity
import com.nahunp.todoapp.data.local.entity.TodoListEntity

/**
 * exportSchema = false, same "not designed yet" honesty as elsewhere in
 * this repo (see CLAUDE.md's Open questions) — this is a pure on-device
 * cache of server state plus a short-lived write queue, not a store of
 * data that only ever lives on the device. A real migration story (rather
 * than DatabaseModule's destructive fallback) is only worth building once
 * losing the local cache on a schema bump actually costs a user something
 * — right now it just means one extra sync pull.
 */
@Database(
    entities = [TodoListEntity::class, TodoItemEntity::class, PendingOperationEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao
    abstract fun todoItemDao(): TodoItemDao
    abstract fun pendingOperationDao(): PendingOperationDao
}
