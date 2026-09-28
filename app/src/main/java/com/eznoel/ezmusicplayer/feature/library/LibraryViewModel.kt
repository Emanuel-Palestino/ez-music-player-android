package com.eznoel.ezmusicplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.data.rawfiles.RawFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: RawFilesRepository
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = repository.observeFiles()
        .map { files ->
            if (files.isEmpty()) LibraryUiState.Empty else LibraryUiState.Content(files)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    init {
        viewModelScope.launch { repository.refreshFiles() }
    }
}
