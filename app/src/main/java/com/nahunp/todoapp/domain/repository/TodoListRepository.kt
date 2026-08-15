package com.nahunp.todoapp.domain.repository

import com.nahunp.todoapp.domain.model.TodoList

interface TodoListRepository {
    suspend fun getTodoLists(): List<TodoList>
    suspend fun getTodoList(id: Int): TodoList
    suspend fun createTodoList(name: String): TodoList
    suspend fun deleteTodoList(id: Int)
    suspend fun addTodoItem(listId: Int, title: String)
    suspend fun completeTodoItem(listId: Int, itemId: Int)
    suspend fun reopenTodoItem(listId: Int, itemId: Int)
    suspend fun removeTodoItem(listId: Int, itemId: Int)
}
