package com.nahunp.todoapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room's standard one-to-many projection — what TodoListDao's Flow queries
 * actually return. No explicit item ordering requested (Room's @Relation
 * doesn't take one); in practice this comes back in rowid order, i.e.
 * ascending by localKey, which tracks creation order closely enough that
 * it hasn't been worth adding an explicit sort column for.
 */
data class TodoListWithItemsEntity(
    @Embedded val list: TodoListEntity,
    @Relation(parentColumn = "localKey", entityColumn = "listLocalKey")
    val items: List<TodoItemEntity>,
)
