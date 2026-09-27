package com.eznoel.ezmusicplayer.feature.files

import com.eznoel.ezmusicplayer.core.model.LibraryFolder

sealed interface FolderConfigUiState {
    data object Loading : FolderConfigUiState
    data object Empty : FolderConfigUiState
    data class Content(
        val folders: List<LibraryFolder>,
        val isRescanning: Boolean = false
    ) : FolderConfigUiState
}