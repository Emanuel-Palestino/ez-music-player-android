package com.eznoel.ezmusicplayer.feature.library

import com.eznoel.ezmusicplayer.core.model.LibrarySummary
import com.eznoel.ezmusicplayer.core.model.Song

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState
    data class Content(
        val songs: List<Song>,
        val summary: LibrarySummary,
    ) : LibraryUiState
}
