package com.nahunp.todoapp.presentation.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.core.network.ApiException
import com.nahunp.todoapp.domain.repository.AuthRepository
import com.nahunp.todoapp.domain.repository.TodoListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoListRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    init {
        // Local-first (see TodoListRepository's doc comment): this Flow is
        // backed by the on-device cache, not a one-shot network call, so
        // it emits immediately with whatever's cached (or empty, on first
        // ever use) and keeps emitting as SyncManager's pulls/pushes
        // update that cache -- no explicit reload() after a mutation
        // needed anymore, unlike the pre-offline-support version of this
        // class.
        viewModelScope.launch {
            repository.observeTodoLists().collect { lists ->
                _uiState.update { it.copy(lists = lists, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.syncStatus.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    fun onNewListNameChange(name: String) = _uiState.update { it.copy(newListName = name) }

    fun createList() {
        val name = _uiState.value.newListName
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createTodoList(name)
            _uiState.update { it.copy(newListName = "") }
        }
    }

    fun renameList(id: Int, newName: String) {
        viewModelScope.launch { repository.renameTodoList(id, newName) }
    }

    fun deleteList(id: Int) {
        viewModelScope.launch { repository.deleteTodoList(id) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }

    fun deleteAccount() {
        _uiState.update { it.copy(deletingAccount = true) }
        viewModelScope.launch {
            try {
                authRepository.deleteAccount()
                authRepository.logout()
                _uiState.update { it.copy(loggedOut = true) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(deletingAccount = false, error = e.message) }
            }
        }
    }
}
