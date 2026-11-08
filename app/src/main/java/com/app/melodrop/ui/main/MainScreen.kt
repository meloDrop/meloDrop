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
import com.app.melodrop.ui.mydrops.MyDropsScreen
import com.app.melodrop.ui.mypicks.MyPicksScreen
import com.app.melodrop.ui.drop.DropScreen
import com.app.melodrop.ui.navigation.BottomBar
import com.app.melodrop.ui.navigation.Routes
import com.app.melodrop.ui.labels.LabelsEditScreen
import com.app.melodrop.ui.podium.PodiumEditScreen
import com.app.melodrop.ui.profile.EditProfileScreen
import com.app.melodrop.ui.profile.ProfileScreen
import com.app.melodrop.ui.profile.UserProfileScreen
import com.app.melodrop.ui.search.UserSearchScreen
import com.app.melodrop.ui.skips.SkipsScreen
import com.app.melodrop.ui.theme.MeloDropTheme
import com.app.melodrop.ui.follows.FollowListScreen
import com.app.melodrop.ui.follows.FollowTab
import com.app.melodrop.ui.tutorial.TutorialOverlay

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Screens opened from a tab: no bottom bar, they have their own back button
    val isSubScreen = currentRoute == Routes.EDIT_PROFILE ||
            currentRoute == Routes.LIKES ||
            currentRoute == Routes.SKIPS ||
            currentRoute == Routes.ACCOUNT ||
            currentRoute == Routes.USER_PROFILE ||
            currentRoute == Routes.SEARCH_USERS ||
            currentRoute == Routes.MY_DROPS ||
            currentRoute == Routes.USER_DROPS ||
            currentRoute == Routes.MY_PICKS ||
            currentRoute == Routes.EDIT_PODIUM ||
            currentRoute == Routes.EDIT_LABELS ||
            currentRoute == Routes.FOLLOWS

    // Halos to show: the search is opened from Discover, so it keeps the Discover halos.
    // The other sub-screens take the halos of the Profile tab
    val haloRoute = when {
        currentRoute == Routes.SEARCH_USERS -> Routes.DISCOVER
        isSubScreen -> Routes.PROFILE
        else -> currentRoute
    }

    // Opens the profile of a user (author of a drop, or search result).
    // launchSingleTop: a double tap does not open the same profile twice
    fun openUserProfile(userId: Int) {
        navController.navigate(Routes.userProfile(userId)) { launchSingleTop = true }
    }

    // Opens the followers or the followed users of a user, from the counters of a profile
    fun openFollows(userId: Int, tab: String) {
        navController.navigate(Routes.follows(userId, tab)) { launchSingleTop = true }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Behind all the screens: the halos move when the tab changes
        HaloBackground(route = haloRoute)

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
                    DiscoverScreen(
                        onAuthorClick = ::openUserProfile,
                        // launchSingleTop: a double tap does not open the search twice
                        onSearchClick = { navController.navigate(Routes.SEARCH_USERS) { launchSingleTop = true } },
                    )
                }
                composable(Routes.DROP) { DropScreen() }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onLogout,
                        onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                        onLikesClick = { navController.navigate(Routes.LIKES) },
                        // launchSingleTop: a double tap does not open these lists twice
                        onDropsClick = { navController.navigate(Routes.MY_DROPS) { launchSingleTop = true } },
                        onSkipsClick = { navController.navigate(Routes.SKIPS) { launchSingleTop = true } },
                        onPicksClick = { navController.navigate(Routes.MY_PICKS) { launchSingleTop = true } },
                        onAccountClick = { navController.navigate(Routes.ACCOUNT) },
                        onEditPodium = { navController.navigate(Routes.EDIT_PODIUM) { launchSingleTop = true } },
                        onEditLabels = { navController.navigate(Routes.EDIT_LABELS) { launchSingleTop = true } },
                        onFollowsClick = ::openFollows,
                        onAuthorClick = ::openUserProfile,
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
                composable(Routes.MY_DROPS) {
                    MyDropsScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                    )
                }
                composable(Routes.MY_PICKS) {
                    MyPicksScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                        onAuthorClick = ::openUserProfile,
                    )
                }
                composable(Routes.EDIT_LABELS) {
                    LabelsEditScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
                    )
                }
                composable(Routes.EDIT_PODIUM) {
                    PodiumEditScreen(
                        // Back to the Profile tab, even after a double tap
                        onBack = { navController.popBackStack(Routes.PROFILE, inclusive = false) },
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
                composable(Routes.SEARCH_USERS) {
                    UserSearchScreen(
                        // Closes the search only: back to Discover
                        onBack = { navController.popBackStack(Routes.SEARCH_USERS, inclusive = true) },
                        onUserClick = ::openUserProfile,
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
                        onFollowsClick = ::openFollows,
                        // "Voir plus" or the "drops" counter.
                        // launchSingleTop: a double tap does not open the list twice
                        onDropsClick = { id ->
                            navController.navigate(Routes.userDrops(id)) { launchSingleTop = true }
                        },
                    )
                }
                composable(
                    route = Routes.USER_DROPS,
                    // {userId} in the route is a number
                    arguments = listOf(navArgument("userId") { type = NavType.IntType }),
                ) { entry ->
                    // Same screen as "Mes drops", for the drops of another user
                    MyDropsScreen(
                        userId = entry.arguments?.getInt("userId"),
                        // Closes this screen only: back to the profile of the user
                        onBack = { navController.popBackStack(Routes.USER_DROPS, inclusive = true) },
                    )
                }
                composable(
                    route = Routes.FOLLOWS,
                    // {userId} is a number, {tab} is "followers" (default) or "following"
                    arguments = listOf(
                        navArgument("userId") { type = NavType.IntType },
                        navArgument("tab") {
                            type = NavType.StringType
                            defaultValue = FollowTab.FOLLOWERS
                        },
                    ),
                ) { entry ->
                    FollowListScreen(
                        userId = entry.arguments?.getInt("userId") ?: 0,
                        initialTab = entry.arguments?.getString("tab") ?: FollowTab.FOLLOWERS,
                        // Closes this screen only, even after a double tap
                        onBack = { navController.popBackStack(Routes.FOLLOWS, inclusive = true) },
                        onUserClick = ::openUserProfile,
                    )
                }
            }
        }

        // Over everything, bottom bar included: the tour shown after an update
        TutorialOverlay()
    }
}


@Preview(showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    MeloDropTheme {
        MainScreen(onLogout = {})
    }
}