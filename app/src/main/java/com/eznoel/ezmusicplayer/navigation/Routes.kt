package com.eznoel.ezmusicplayer.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.eznoel.ezmusicplayer.R
import kotlinx.serialization.Serializable

@Serializable data object LibraryRoute
@Serializable data object PlaylistsRoute
@Serializable data object SettingsRoute

enum class TopLevelDestination(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    LIBRARY(LibraryRoute, R.string.nav_library, R.drawable.ic_library_music),
    PLAYLISTS(PlaylistsRoute, R.string.nav_playlists, R.drawable.ic_queue_music),
    SEARCH(SettingsRoute, R.string.nav_settings, R.drawable.ic_settings),
}