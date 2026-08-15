package com.nahunp.todoapp.data.local

import com.nahunp.todoapp.data.local.entity.TodoItemEntity
import com.nahunp.todoapp.data.local.entity.TodoListWithItemsEntity
import com.nahunp.todoapp.data.remote.dto.TodoItemDto
import com.nahunp.todoapp.data.remote.dto.TodoListDto
import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.DueDateState
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.model.TodoItem
import com.nahunp.todoapp.domain.model.TodoList
import java.time.Instant

/**
 * Local-cache -> domain (what TodoListRepositoryImpl's Flow reads expose),
 * and network DTO -> sync snapshot (what SyncManager's pull step feeds
 * into LocalTodoDataSource.mergeServerState). This is the "what used to be
 * one direct DTO -> domain hop, pre-offline-support, is now two separate
 * hops with the local cache in between" mapping layer.
 */

fun TodoListWithItemsEntity.toDomain(): TodoList = TodoList(
    id = list.localKey,
    name = list.name,
    items = items.map { it.toDomain() },
)

fun TodoItemEntity.toDomain(): TodoItem = TodoItem(
    id = localKey,
    title = title,
    notes = notes,
    isDone = isDone,
    completedAt = completedAt?.let { Instant.parse(it) },
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { Instant.parse(it) },
    dueDateState = DueDateState.valueOf(dueDateState),
    category = Category.valueOf(category),
)

fun TodoListDto.toSnapshot() = ServerListSnapshot(
    serverId = id,
    name = name,
    items = items.map { it.toSnapshot() },
)

fun TodoItemDto.toSnapshot() = ServerItemSnapshot(
    serverId = id,
    title = title,
    notes = notes,
    isDone = isDone,
    completedAt = completedAt,
    priority = priority,
    dueDate = dueDate,
    dueDateState = dueDateState,
    category = category,
)
