package com.app.melodrop.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.melodrop.ui.navigation.Routes

// Screens shown before login: login and sign-up
@Composable
fun AuthNavHost(onLoggedIn: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = onLoggedIn,
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = onLoggedIn,
                onLoginClick = { navController.popBackStack() },
            )
        }
    }
}