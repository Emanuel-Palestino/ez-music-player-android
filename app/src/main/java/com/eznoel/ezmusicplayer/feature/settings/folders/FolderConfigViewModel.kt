package com.eznoel.ezmusicplayer.feature.settings.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.data.rawfiles.RawFilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderConfigViewModel @Inject constructor(
    private val repository: RawFilesRepository
) : ViewModel() {

    private val isRescanning = MutableStateFlow(false)

    val uiState: StateFlow<FolderConfigUiState> = combine(
        repository.observeFolders(),
        isRescanning
    ) { folders, rescanning ->
        when {
            folders.isEmpty() && !rescanning -> FolderConfigUiState.Empty
            else -> FolderConfigUiState.Content(folders, isRescanning = rescanning)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FolderConfigUiState.Loading
    )

    init {
        viewModelScope.launch { repository.refreshFolders() }
    }

    fun onFolderToggled(relativePath: String, included: Boolean) {
        viewModelScope.launch { repository.setFolderIncluded(relativePath, included) }
    }

    fun onRescanRequested() {
        viewModelScope.launch {
            isRescanning.value = true
            repository.forceRescan()
            isRescanning.value = false
        }
    }
}