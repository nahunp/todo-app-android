package com.nahunp.todoapp.domain.repository

import com.nahunp.todoapp.domain.model.Category
import com.nahunp.todoapp.domain.model.Priority
import com.nahunp.todoapp.domain.model.TodoList
import java.time.Instant

interface TodoListRepository {
    suspend fun getTodoLists(): List<TodoList>
    suspend fun getTodoList(id: Int): TodoList
    suspend fun createTodoList(name: String): TodoList
    suspend fun renameTodoList(id: Int, newName: String)
    suspend fun deleteTodoList(id: Int)
    suspend fun addTodoItem(listId: Int, title: String)
    suspend fun renameTodoItem(listId: Int, itemId: Int, newTitle: String)
    suspend fun setTodoItemPriority(listId: Int, itemId: Int, priority: Priority)
    suspend fun setTodoItemCategory(listId: Int, itemId: Int, category: Category)
    suspend fun setTodoItemDueDate(listId: Int, itemId: Int, dueDate: Instant?)
    suspend fun completeTodoItem(listId: Int, itemId: Int)
    suspend fun reopenTodoItem(listId: Int, itemId: Int)
    suspend fun removeTodoItem(listId: Int, itemId: Int)
}
