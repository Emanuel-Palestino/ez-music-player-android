package com.eznoel.ezmusicplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.data.library.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repository.observeFolders()
        .map { folders ->
            SettingsUiState.Content(
                folderCount = folders.size,
                excludedCount = folders.count { !it.isIncluded },
                includedSizeBytes = folders.filter { it.isIncluded }.sumOf { it.totalSizeBytes }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading
        )
}