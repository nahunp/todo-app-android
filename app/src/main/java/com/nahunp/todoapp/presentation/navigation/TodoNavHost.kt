package com.nahunp.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nahunp.todoapp.BuildConfig
import com.nahunp.todoapp.presentation.auth.login.LoginScreen
import com.nahunp.todoapp.presentation.auth.register.RegisterScreen
import com.nahunp.todoapp.presentation.legal.LegalWebViewScreen
import com.nahunp.todoapp.presentation.todolist.TodoListScreen
import com.nahunp.todoapp.presentation.todolist.detail.TodoListDetailScreen

/**
 * startDestination is resolved once by AppEntryViewModel (see its doc
 * comment for why this is a one-shot check, not a full route-guard
 * equivalent of the web frontend's authGuard) and passed in by
 * MainActivity — never hardcoded here.
 */
@Composable
fun TodoNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.TodoLists.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Destination.Register.route) },
                onOpenTerms = { navController.navigate(Destination.Terms.route) },
                onOpenPrivacy = { navController.navigate(Destination.Privacy.route) },
            )
        }
        composable(Destination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onOpenTerms = { navController.navigate(Destination.Terms.route) },
                onOpenPrivacy = { navController.navigate(Destination.Privacy.route) },
            )
        }
        composable(Destination.Terms.route) {
            LegalWebViewScreen(
                title = "Terms of Service",
                url = BuildConfig.TERMS_URL,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destination.Privacy.route) {
            LegalWebViewScreen(
                title = "Privacy Policy",
                url = BuildConfig.PRIVACY_URL,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destination.TodoLists.route) {
            TodoListScreen(
                onOpenList = { listId -> navController.navigate(Destination.TodoListDetail.createRoute(listId)) },
                onLoggedOut = {
                    navController.navigate(Destination.Login.route) {
                        // Clear the whole back stack, not just TodoLists —
                        // otherwise pressing back after logout could land
                        // on a detail screen for data that's no longer
                        // this (now logged-out) user's to see.
                        popUpTo(0)
                    }
                },
            )
        }
        composable(
            route = Destination.TodoListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.IntType }),
        ) {
            TodoListDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
