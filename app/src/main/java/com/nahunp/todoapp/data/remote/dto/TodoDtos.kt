package com.nahunp.todoapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TodoListDto(
    val id: Int,
    val name: String,
    val items: List<TodoItemDto> = emptyList(),
)

// Field names checked against docs/api/openapi.json in the web repo, not
// guessed — `isDone`, not `isComplete` (a real bug caught and fixed after
// the initial scaffold assumed the latter). priority/category/dueDateState
// arrive as their string enum names ("High", "Work", "Overdue"...), not
// raw ints — the backend's JsonStringEnumConverter (Program.cs) guarantees
// this. kotlinx.serialization needs the Kotlin enum's constant names to
// match exactly (case-sensitive) for this to deserialize without a custom
// serializer — see domain/model/TodoItem.kt's enums.
@Serializable
data class TodoItemDto(
    val id: Int,
    val title: String,
    val notes: String?,
    val isDone: Boolean,
    val completedAt: String?,
    val priority: String,
    val dueDate: String?,
    val category: String,
    val dueDateState: String,
)

// AddTodoItemRequest on the backend has notes/priority/dueDate/category
// all optional with server-side defaults (Notes=null, Priority=Medium,
// DueDate=null, Category=None) — omitting them here and letting
// System.Text.Json apply those defaults is fine, no need to send every
// field just because the C# record declares them.
@Serializable
data class CreateTodoItemRequestDto(val title: String)

@Serializable
data class CreateTodoListRequestDto(val name: String)

@Serializable
data class RenameTodoListRequestDto(val newName: String)

@Serializable
data class RenameTodoItemRequestDto(val newTitle: String)

@Serializable
data class SetPriorityRequestDto(val priority: String)

@Serializable
data class SetCategoryRequestDto(val category: String)

@Serializable
data class SetDueDateRequestDto(val dueDate: String?)

// POST /todolists and POST /todolists/{id}/items both return
// `Results.Created(..., new { id })` — just the new resource's id, not
// the full object. See TodoListEndpoints.cs.
@Serializable
data class IdResponseDto(val id: Int)
