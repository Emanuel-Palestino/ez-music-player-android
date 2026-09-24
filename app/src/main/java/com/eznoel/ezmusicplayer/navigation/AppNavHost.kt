package com.eznoel.ezmusicplayer.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eznoel.ezmusicplayer.feature.library.LibraryScreen
import com.eznoel.ezmusicplayer.feature.library.PlaylistsScreen
import com.eznoel.ezmusicplayer.feature.library.SettingsScreen

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
        composable<LibraryRoute> { LibraryScreen() }
        composable<PlaylistsRoute> { PlaylistsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
    }
}