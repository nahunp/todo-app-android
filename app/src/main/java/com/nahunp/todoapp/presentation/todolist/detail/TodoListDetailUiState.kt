package com.nahunp.todoapp.presentation.todolist.detail

import com.nahunp.todoapp.domain.model.SyncStatus
import com.nahunp.todoapp.domain.model.TodoList

// No `error` field -- unlike before offline support, mutations on this
// screen no longer throw synchronously (see TodoListRepository's doc
// comment), so there's nothing for a per-action error message to show.
// SyncStatusLine (driven by `syncStatus` below) is where a sync failure
// eventually surfaces instead.
data class TodoListDetailUiState(
    val list: TodoList? = null,
    val isLoading: Boolean = true,
    val newItemTitle: String = "",
    val syncStatus: SyncStatus = SyncStatus(),
)
