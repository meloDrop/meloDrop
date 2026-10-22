package com.app.melodrop.ui.main

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.app.melodrop.ui.components.HaloBackground
import com.app.melodrop.ui.discover.DiscoverScreen
import com.app.melodrop.ui.drop.DropScreen
import com.app.melodrop.ui.navigation.BottomBar
import com.app.melodrop.ui.navigation.Routes
import com.app.melodrop.ui.profile.EditProfileScreen
import com.app.melodrop.ui.profile.ProfileScreen
import com.app.melodrop.ui.theme.MeloDropTheme

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        // Behind all the screens: the halos move when the tab changes.
        // The edit screen keeps the halos of the Profile tab
        HaloBackground(
            route = if (currentRoute == Routes.EDIT_PROFILE) Routes.PROFILE else currentRoute,
        )

        Scaffold(
            // Transparent, so the halos show through
            containerColor = Color.Transparent,
            bottomBar = {
                // No bottom bar on the edit screen: it has its own back button
                if (currentRoute != Routes.EDIT_PROFILE) {
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
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DISCOVER,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.DISCOVER) { DiscoverScreen() }
                composable(Routes.DROP) { DropScreen() }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    )
                }
                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                    )
                }
            }
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