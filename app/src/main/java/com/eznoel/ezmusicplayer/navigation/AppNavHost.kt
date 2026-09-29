package com.eznoel.ezmusicplayer.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.eznoel.ezmusicplayer.feature.settings.folders.FolderConfigScreen
import com.eznoel.ezmusicplayer.feature.tagedit.EditTagsScreen
import com.eznoel.ezmusicplayer.feature.library.LibraryScreen
import com.eznoel.ezmusicplayer.feature.library.PlaylistsScreen
import com.eznoel.ezmusicplayer.feature.library.SettingsScreen
import com.eznoel.ezmusicplayer.feature.nowplaying.NowPlayingScreen

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = LibraryRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(90)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { fadeOut(tween(90)) },
    ) {
        composable<LibraryRoute> {
            LibraryScreen(
                onNavigateToEditTags = { route -> navController.navigate(route) }
            )
        }
        composable<PlaylistsRoute> { PlaylistsScreen() }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateToFolderConfig = { navController.navigate(FolderConfigRoute) },
            )
        }
        composable<FolderConfigRoute> {
            FolderConfigScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<EditTagsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<EditTagsRoute>()
            EditTagsScreen(
                route = route,
                onClose = { navController.navigateUp() }
            )
        }

        composable<NowPlayingRoute>(
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) },
            popEnterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) },
        ) {
            NowPlayingScreen(
                onClose = { navController.navigateUp() },
                onNavigateToEditTags = { navController.navigate(it) },
            )
        }
    }
}