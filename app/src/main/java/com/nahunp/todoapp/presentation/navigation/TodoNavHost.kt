package com.nahunp.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nahunp.todoapp.presentation.auth.login.LoginScreen
import com.nahunp.todoapp.presentation.auth.register.RegisterScreen
import com.nahunp.todoapp.presentation.todolist.TodoListScreen

/**
 * No auth guard equivalent of the web frontend's authGuard yet (functional
 * route guard redirecting to /login — see the web repo's
 * services/auth.guard.ts) — this just always starts at Login. Worth adding
 * once there's a real "check AuthRepository.isAuthenticated on launch,
 * skip straight to TodoLists if already logged in" flow; not done here so
 * this stays a template rather than a finished decision.
 */
@Composable
fun TodoNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destination.Login.route) {
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.TodoLists.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Destination.Register.route) },
            )
        }
        composable(Destination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
            )
        }
        composable(Destination.TodoLists.route) {
            TodoListScreen(
                onOpenList = { /* TODO: navController.navigate(Destination.TodoListDetail.createRoute(it)) once a detail screen exists */ },
            )
        }
    }
}
