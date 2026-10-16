package com.app.melodrop.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.melodrop.ui.discover.DiscoverScreen
import com.app.melodrop.ui.drop.DropScreen
import com.app.melodrop.ui.navigation.BottomBar
import com.app.melodrop.ui.navigation.Routes
import com.app.melodrop.ui.profile.ProfileScreen
import com.app.melodrop.ui.theme.MeloDropTheme

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = currentRoute,
                onTabClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DISCOVER,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.DISCOVER) { DiscoverScreen() }
            composable(Routes.DROP) { DropScreen() }
            composable(Routes.PROFILE) { ProfileScreen(onLogout = onLogout) }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    MeloDropTheme {
        MainScreen(onLogout = {})
    }
}