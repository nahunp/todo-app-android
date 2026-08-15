package com.nahunp.todoapp.presentation.todolist.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.repository.TodoListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodoListDetailViewModel @Inject constructor(
    private val repository: TodoListRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Populated automatically by Navigation Compose's Hilt integration from
    // the {listId} nav argument (see TodoNavHost's navArgument declaration)
    // — the local cache's stable key (TodoListEntity.localKey), not a
    // server id; see TodoListRepositoryImpl's class doc comment. Survives
    // process death/recreation with the same value either way.
    private val listId: Int = checkNotNull(savedStateHandle["listId"])

    private val _uiState = MutableStateFlow(TodoListDetailUiState())
    val uiState: StateFlow<TodoListDetailUiState> = _uiState.asStateFlow()

    init {
        // Local-first, same as TodoListViewModel: this Flow is keyed on
        // listId's stable local key, so it keeps observing the same row
        // correctly through a background sync -- no explicit reload()
        // after a mutation needed anymore.
        viewModelScope.launch {
            repository.observeTodoList(listId).collect { list ->
                _uiState.update { it.copy(list = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.syncStatus.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    fun onNewItemTitleChange(title: String) = _uiState.update { it.copy(newItemTitle = title) }

    fun addItem() {
        val title = _uiState.value.newItemTitle
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTodoItem(listId, title)
            _uiState.update { it.copy(newItemTitle = "") }
        }
    }

    fun renameList(newName: String) {
        viewModelScope.launch { repository.renameTodoList(listId, newName) }
    }

    fun renameItem(itemId: Int, newTitle: String) {
        viewModelScope.launch { repository.renameTodoItem(listId, itemId, newTitle) }
    }

    fun toggleDone(itemId: Int, currentlyDone: Boolean) {
        viewModelScope.launch {
            if (currentlyDone) repository.reopenTodoItem(listId, itemId) else repository.completeTodoItem(listId, itemId)
        }
    }

    fun removeItem(itemId: Int) {
        viewModelScope.launch { repository.removeTodoItem(listId, itemId) }
    }

    // Simple tap-to-cycle rather than a dropdown/menu — fewer moving parts
    // for a first pass. A real picker (dropdown for category, a proper
    // segmented control for priority) is a reasonable follow-up, not a must
    // for this to be useful.
    fun cyclePriority(itemId: Int, current: Priority) {
        val next = Priority.entries[(current.ordinal + 1) % Priority.entries.size]
        viewModelScope.launch { repository.setTodoItemPriority(listId, itemId, next) }
    }

    fun cycleCategory(itemId: Int, current: Category) {
        val next = Category.entries[(current.ordinal + 1) % Category.entries.size]
        viewModelScope.launch { repository.setTodoItemCategory(listId, itemId, next) }
    }

    fun setDueDate(itemId: Int, dueDate: Instant?) {
        viewModelScope.launch { repository.setTodoItemDueDate(listId, itemId, dueDate) }
    }
}
