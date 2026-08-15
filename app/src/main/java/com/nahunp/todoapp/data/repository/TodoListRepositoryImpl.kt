package com.nahunp.todoapp.data.repository

import com.nahunp.todoapp.core.network.TodoApiService
import com.nahunp.todoapp.core.network.toApiException
import com.nahunp.todoapp.data.remote.dto.CreateTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.CreateTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.RenameTodoListRequestDto
import com.nahunp.todoapp.data.remote.dto.SetCategoryRequestDto
import com.nahunp.todoapp.data.remote.dto.SetDueDateRequestDto
import com.nahunp.todoapp.data.remote.dto.SetPriorityRequestDto
import com.nahunp.todoapp.data.remote.dto.TodoItemDto
import com.nahunp.todoapp.data.remote.dto.TodoListDto
import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.DueDateState
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.model.TodoItem
import com.nahunp.todoapp.domain.model.TodoList
import com.nahunp.todoapp.domain.repository.TodoListRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoListRepositoryImpl @Inject constructor(
    private val api: TodoApiService,
) : TodoListRepository {

    override suspend fun getTodoLists(): List<TodoList> = wrap { api.getTodoLists().map { it.toDomain() } }

    override suspend fun getTodoList(id: Int): TodoList = wrap { api.getTodoList(id).toDomain() }

    override suspend fun createTodoList(name: String): TodoList = wrap {
        // POST /todolists only returns the new id (see TodoApiService's
        // doc comment) — construct the rest client-side from what was
        // just sent rather than making a second round-trip to re-fetch it.
        val response = api.createTodoList(CreateTodoListRequestDto(name))
        TodoList(id = response.id, name = name, items = emptyList())
    }

    override suspend fun renameTodoList(id: Int, newName: String) =
        wrap { api.renameTodoList(id, RenameTodoListRequestDto(newName)) }

    override suspend fun deleteTodoList(id: Int) = wrap { api.deleteTodoList(id) }

    override suspend fun addTodoItem(listId: Int, title: String) {
        wrap { api.addTodoItem(listId, CreateTodoItemRequestDto(title)) }
    }

    override suspend fun renameTodoItem(listId: Int, itemId: Int, newTitle: String) =
        wrap { api.renameTodoItem(listId, itemId, RenameTodoItemRequestDto(newTitle)) }

    override suspend fun setTodoItemPriority(listId: Int, itemId: Int, priority: Priority) =
        wrap { api.setTodoItemPriority(listId, itemId, SetPriorityRequestDto(priority.name)) }

    override suspend fun setTodoItemCategory(listId: Int, itemId: Int, category: Category) =
        wrap { api.setTodoItemCategory(listId, itemId, SetCategoryRequestDto(category.name)) }

    override suspend fun setTodoItemDueDate(listId: Int, itemId: Int, dueDate: Instant?) =
        wrap { api.setTodoItemDueDate(listId, itemId, SetDueDateRequestDto(dueDate?.toString())) }

    override suspend fun completeTodoItem(listId: Int, itemId: Int) = wrap { api.completeTodoItem(listId, itemId) }

    override suspend fun reopenTodoItem(listId: Int, itemId: Int) = wrap { api.reopenTodoItem(listId, itemId) }

    override suspend fun removeTodoItem(listId: Int, itemId: Int) = wrap { api.removeTodoItem(listId, itemId) }

    private suspend fun <T> wrap(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            throw t.toApiException()
        }
}

private fun TodoListDto.toDomain() = TodoList(id = id, name = name, items = items.map { it.toDomain() })

private fun TodoItemDto.toDomain() = TodoItem(
    id = id,
    title = title,
    notes = notes,
    isDone = isDone,
    completedAt = completedAt?.let { Instant.parse(it) },
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { Instant.parse(it) },
    dueDateState = DueDateState.valueOf(dueDateState),
    category = Category.valueOf(category),
)
