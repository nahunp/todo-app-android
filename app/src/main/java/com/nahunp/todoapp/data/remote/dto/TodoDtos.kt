package com.nahunp.todoapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TodoListDto(
    val id: Int,
    val name: String,
    val items: List<TodoItemDto> = emptyList(),
)

// priority/category/dueDateState arrive as their string enum names
// ("High", "Work", "Overdue"...), not raw ints — the backend's
// JsonStringEnumConverter (Program.cs) guarantees this. kotlinx.serialization
// needs the Kotlin enum's constant names to match exactly (case-sensitive)
// for this to deserialize without a custom serializer — see
// domain/model/TodoItem.kt's enums, which are deliberately spelled to match.
@Serializable
data class TodoItemDto(
    val id: Int,
    val title: String,
    val isComplete: Boolean,
    val priority: String,
    val dueDate: String?,
    val dueDateState: String,
    val category: String,
)

@Serializable
data class CreateTodoListRequestDto(val name: String)

@Serializable
data class CreateTodoItemRequestDto(val title: String, val category: String = "None")
