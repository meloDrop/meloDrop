package com.app.melodrop.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.melodrop.ui.navigation.Routes

// Screens shown before login: login, sign-up and email check
@Composable
fun AuthNavHost(onLoggedIn: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = onLoggedIn,
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onVerifyNeeded = { email -> navController.navigate(Routes.verifyEmail(email)) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = { email ->
                    navController.navigate(Routes.verifyEmail(email)) {
                        // The account exists now: Back must not reopen the sign-up form
                        popUpTo(Routes.LOGIN)
                    }
                },
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.VERIFY_EMAIL,
            // Optional: empty when login was done with a username
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
        ) { backStackEntry ->
            VerifyEmailScreen(
                initialEmail = backStackEntry.arguments?.getString("email") ?: "",
                // Goes back to login, whatever the path that led here
                onLoginClick = { navController.popBackStack(Routes.LOGIN, inclusive = false) },
            )
        }
    }
}