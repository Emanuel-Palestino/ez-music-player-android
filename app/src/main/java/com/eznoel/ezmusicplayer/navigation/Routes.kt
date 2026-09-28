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
@Serializable data object FolderConfigRoute
@Serializable data object SettingsRoute

@Serializable data class EditTagsRoute(val uriString: String, val fileName: String, val relativePath: String)

enum class TopLevelDestination(
    val route: Any,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    LIBRARY(LibraryRoute, R.string.nav_library, Icons.Rounded.LibraryMusic),
    PLAYLISTS(PlaylistsRoute, R.string.nav_playlists, Icons.AutoMirrored.Rounded.QueueMusic),
    SETTINGS(SettingsRoute, R.string.nav_settings, Icons.Rounded.Settings),
}