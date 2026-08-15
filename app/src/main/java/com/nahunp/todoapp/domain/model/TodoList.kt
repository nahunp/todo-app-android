package com.nahunp.todoapp.domain.model

data class TodoList(
    val id: Int,
    val name: String,
    val items: List<TodoItem> = emptyList(),
)
