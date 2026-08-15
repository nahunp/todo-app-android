package com.nahunp.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Same `localKey`-is-stable / `serverId`-is-sync-internal split as
 * TodoListEntity — see its doc comment. `listLocalKey` points at the
 * parent list's stable key, not its server id, so it never needs updating
 * regardless of sync state (only `onDelete = CASCADE` is needed here, no
 * `onUpdate` — `localKey` values never change once assigned).
 *
 * Fields stored as raw strings for priority/category/dueDateState —
 * mirrors the enum-name-over-the-wire convention TodoDtos.kt already uses
 * (see that file's doc comment) rather than adding Room TypeConverters;
 * mapping to/from the domain enums happens at the repository boundary,
 * same place TodoListRepositoryImpl already did it for the network DTOs.
 *
 * `dueDateState` is normally server-computed (see domain/model/TodoItem.kt
 * — "never stored, never computed client-side"). It's stored here anyway
 * as a *local approximation*, recomputed on-device (device clock/timezone,
 * see TodoListRepositoryImpl.computeDueDateStateLocally) only for optimistic
 * offline display, and gets overwritten with the real value on the next
 * successful sync pull. Known, accepted imprecision — see CLAUDE.md.
 */
@Entity(
    tableName = "todo_items",
    foreignKeys = [
        ForeignKey(
            entity = TodoListEntity::class,
            parentColumns = ["localKey"],
            childColumns = ["listLocalKey"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("listLocalKey")],
)
data class TodoItemEntity(
    @PrimaryKey(autoGenerate = true) val localKey: Int = 0,
    val listLocalKey: Int,
    val serverId: Int? = null,
    val title: String,
    val notes: String?,
    val isDone: Boolean,
    val completedAt: String?,
    val priority: String,
    val dueDate: String?,
    val dueDateState: String,
    val category: String,
)
