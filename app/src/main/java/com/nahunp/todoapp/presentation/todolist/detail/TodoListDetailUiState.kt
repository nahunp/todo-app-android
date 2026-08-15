package com.nahunp.todoapp.presentation.todolist.detail

import com.nahunp.todoapp.domain.model.TodoList

data class TodoListDetailUiState(
    val list: TodoList? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val newItemTitle: String = "",
)
