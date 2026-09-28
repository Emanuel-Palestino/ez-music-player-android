package com.eznoel.ezmusicplayer.feature.tagedit

sealed interface EditTagsUiState {
    data object Loading : EditTagsUiState
    data class Content(
        val form: TagFormState,
        val fileName: String,
        val relativePath: String,
        val isSaving: Boolean = false,
        val errorMessage: String? = null
    ) : EditTagsUiState
}