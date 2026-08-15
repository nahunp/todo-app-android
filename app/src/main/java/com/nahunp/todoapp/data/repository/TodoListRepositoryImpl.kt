package com.nahunp.todoapp.data.repository

import com.nahunp.todoapp.core.network.TodoApiService
import com.nahunp.todoapp.core.network.toApiException
import com.nahunp.todoapp.data.remote.dto.CreateTodoItemRequestDto
import com.nahunp.todoapp.data.remote.dto.CreateTodoListRequestDto
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

    override suspend fun createTodoList(name: String): TodoList =
        wrap { api.createTodoList(CreateTodoListRequestDto(name)).toDomain() }

    override suspend fun deleteTodoList(id: Int) = wrap { api.deleteTodoList(id) }

    override suspend fun addTodoItem(listId: Int, title: String) =
        wrap { api.addTodoItem(listId, CreateTodoItemRequestDto(title)) }

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
    isComplete = isComplete,
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { Instant.parse(it) },
    dueDateState = DueDateState.valueOf(dueDateState),
    category = Category.valueOf(category),
)
