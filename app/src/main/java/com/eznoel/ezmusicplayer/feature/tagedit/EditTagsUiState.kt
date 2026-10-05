package com.eznoel.ezmusicplayer.feature.tagedit

import com.eznoel.ezmusicplayer.core.model.Song

sealed interface EditTagsUiState {
    data object Loading : EditTagsUiState
    data class Content(
        val song: Song? = null,
        val form: TagFormState,
        val fileName: String,
        val relativePath: String,
        val format: String = "",
        val fileSizeBytes: Long = 0,
        val isLoadingTags: Boolean = true,
        val isSaving: Boolean = false,
        val errorMessage: String? = null
    ) : EditTagsUiState
}