package com.nahunp.todoapp.presentation.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.core.network.ApiException
import com.nahunp.todoapp.domain.repository.TodoListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoListRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val lists = repository.getTodoLists()
                _uiState.update { it.copy(lists = lists, isLoading = false) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNewListNameChange(name: String) = _uiState.update { it.copy(newListName = name) }

    fun createList() {
        val name = _uiState.value.newListName
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.createTodoList(name)
                _uiState.update { it.copy(newListName = "") }
                load()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteList(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTodoList(id)
                load()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
