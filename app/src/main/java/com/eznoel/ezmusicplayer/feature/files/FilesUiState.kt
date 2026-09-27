package com.eznoel.ezmusicplayer.feature.files

import com.eznoel.ezmusicplayer.core.model.RawAudioFile

sealed interface FilesUiState {
    data object Loading : FilesUiState
    data object Empty : FilesUiState
    data class Content(val files: List<RawAudioFile>) : FilesUiState
}