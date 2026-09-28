package com.eznoel.ezmusicplayer.feature.library

import com.eznoel.ezmusicplayer.core.model.RawAudioFile

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState
    data class Content(val files: List<RawAudioFile>) : LibraryUiState
}
