package com.nahunp.todoapp.presentation.navigation

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Register : Destination("register")
    data object TodoLists : Destination("todo_lists")
    data object TodoListDetail : Destination("todo_lists/{listId}") {
        fun createRoute(listId: Int) = "todo_lists/$listId"
    }
    data object Terms : Destination("terms")
    data object Privacy : Destination("privacy")
}
