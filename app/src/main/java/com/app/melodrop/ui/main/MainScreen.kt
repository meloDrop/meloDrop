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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.melodrop.ui.account.AccountScreen
import com.app.melodrop.ui.components.HaloBackground
import com.app.melodrop.ui.discover.DiscoverScreen
import com.app.melodrop.ui.likes.LikesScreen
import com.app.melodrop.ui.drop.DropScreen
import com.app.melodrop.ui.navigation.BottomBar
import com.app.melodrop.ui.navigation.Routes
import com.app.melodrop.ui.profile.EditProfileScreen
import com.app.melodrop.ui.profile.ProfileScreen
import com.app.melodrop.ui.profile.UserProfileScreen
import com.app.melodrop.ui.skips.SkipsScreen
import com.app.melodrop.ui.theme.MeloDropTheme

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Screens opened from a tab: no bottom bar, and the halos of the Profile tab
    val isSubScreen = currentRoute == Routes.EDIT_PROFILE ||
            currentRoute == Routes.LIKES ||
            currentRoute == Routes.SKIPS ||
            currentRoute == Routes.ACCOUNT ||
            currentRoute == Routes.USER_PROFILE

    // Opens the profile of the author of a drop. launchSingleTop: a double tap
    // does not open the same profile twice
    fun openUserProfile(userId: Int) {
        navController.navigate(Routes.userProfile(userId)) { launchSingleTop = true }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Behind all the screens: the halos move when the tab changes
        HaloBackground(route = if (isSubScreen) Routes.PROFILE else currentRoute)

        Scaffold(
            // Transparent, so the halos show through
            containerColor = Color.Transparent,
            bottomBar = {
                // These screens have their own back button
                if (!isSubScreen) {
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
                composable(Routes.DISCOVER) {
                    DiscoverScreen(onAuthorClick = ::openUserProfile)
                }
                composable(Routes.DROP) { DropScreen() }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                        onLikesClick = { navController.navigate(Routes.LIKES) },
                        // launchSingleTop: a double tap does not open the list twice
                        onSkipsClick = { navController.navigate(Routes.SKIPS) { launchSingleTop = true } },
                        onAccountClick = { navController.navigate(Routes.ACCOUNT) },
                    )
                }
                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                    )
                }
                composable(Routes.LIKES) {
                    LikesScreen(
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                        onAuthorClick = ::openUserProfile,
                    )
                }
                composable(Routes.SKIPS) {
                    SkipsScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                        onAuthorClick = ::openUserProfile,
                    )
                }
                composable(Routes.ACCOUNT) {
                    AccountScreen(
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                        // Account deleted: same as a logout, back to the login screen
                        onAccountDeleted = onLogout,
                    )
                }
                composable(
                    route = Routes.USER_PROFILE,
                    // {userId} in the route is a number
                    arguments = listOf(navArgument("userId") { type = NavType.IntType }),
                ) { entry ->
                    UserProfileScreen(
                        userId = entry.arguments?.getInt("userId") ?: 0,
                        // Closes this screen only, even after a double tap
                        onBack = { navController.popBackStack(Routes.USER_PROFILE, inclusive = true) },
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