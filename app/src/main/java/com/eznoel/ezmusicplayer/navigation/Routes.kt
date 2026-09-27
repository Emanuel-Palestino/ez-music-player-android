package com.eznoel.ezmusicplayer.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.eznoel.ezmusicplayer.R
import kotlinx.serialization.Serializable

@Serializable data object LibraryRoute
@Serializable data object PlaylistsRoute
@Serializable data object FilesRoute
@Serializable data object SettingsRoute

enum class TopLevelDestination(
    val route: Any,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    LIBRARY(LibraryRoute, R.string.nav_library, Icons.Rounded.LibraryMusic),
    PLAYLISTS(PlaylistsRoute, R.string.nav_playlists, Icons.AutoMirrored.Rounded.QueueMusic),
    FILES(FilesRoute, R.string.nav_files, Icons.Rounded.Folder),
    SETTINGS(SettingsRoute, R.string.nav_settings, Icons.Rounded.Settings),
}