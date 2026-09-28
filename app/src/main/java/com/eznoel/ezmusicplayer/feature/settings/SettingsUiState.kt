package com.eznoel.ezmusicplayer.feature.settings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Content(
        val folderCount: Int,
        val excludedCount: Int,
        val includedSizeBytes: Long
    ) : SettingsUiState
}