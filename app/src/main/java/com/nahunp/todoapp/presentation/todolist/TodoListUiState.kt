package com.nahunp.todoapp.presentation.todolist

import com.nahunp.todoapp.domain.model.TodoList

data class TodoListUiState(
    val lists: List<TodoList> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val newListName: String = "",
    val loggedOut: Boolean = false,
)
