package com.nahunp.todoapp.presentation.todolist.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.core.network.ApiException
import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.repository.TodoListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TodoListDetailViewModel @Inject constructor(
    private val repository: TodoListRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Populated automatically by Navigation Compose's Hilt integration from
    // the {listId} nav argument (see TodoNavHost's navArgument declaration)
    // — not read from an Intent extra or a manually-passed constructor
    // param, so this ViewModel survives process death/recreation with the
    // same listId intact.
    private val listId: Int = checkNotNull(savedStateHandle["listId"])

    private val _uiState = MutableStateFlow(TodoListDetailUiState())
    val uiState: StateFlow<TodoListDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.getTodoList(listId)
                _uiState.update { it.copy(list = list, isLoading = false) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNewItemTitleChange(title: String) = _uiState.update { it.copy(newItemTitle = title) }

    fun addItem() {
        val title = _uiState.value.newItemTitle
        if (title.isBlank()) return
        runAndReload {
            repository.addTodoItem(listId, title)
            _uiState.update { it.copy(newItemTitle = "") }
        }
    }

    fun toggleDone(itemId: Int, currentlyDone: Boolean) = runAndReload {
        if (currentlyDone) repository.reopenTodoItem(listId, itemId) else repository.completeTodoItem(listId, itemId)
    }

    fun removeItem(itemId: Int) = runAndReload { repository.removeTodoItem(listId, itemId) }

    // Simple tap-to-cycle rather than a dropdown/menu — fewer moving parts
    // for a first pass. A real picker (dropdown for category, a proper
    // segmented control for priority) is a reasonable follow-up, not a must
    // for this to be useful.
    fun cyclePriority(itemId: Int, current: Priority) {
        val next = Priority.entries[(current.ordinal + 1) % Priority.entries.size]
        runAndReload { repository.setTodoItemPriority(listId, itemId, next) }
    }

    fun cycleCategory(itemId: Int, current: Category) {
        val next = Category.entries[(current.ordinal + 1) % Category.entries.size]
        runAndReload { repository.setTodoItemCategory(listId, itemId, next) }
    }

    fun setDueDate(itemId: Int, dueDate: Instant?) = runAndReload {
        repository.setTodoItemDueDate(listId, itemId, dueDate)
    }

    private fun runAndReload(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                load()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
