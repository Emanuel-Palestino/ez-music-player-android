package com.eznoel.ezmusicplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.core.model.Song
import com.eznoel.ezmusicplayer.data.library.LibraryRepository
import com.eznoel.ezmusicplayer.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeSongs(),
        repository.observeSummary(),
        repository.isSyncing,
    ) { songs, summary, syncing ->
        when {
            songs.isEmpty() && syncing -> LibraryUiState.Loading
            songs.isEmpty() -> LibraryUiState.Empty
            else -> LibraryUiState.Content(songs, summary)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState.Loading,
    )

    fun onScreenReady() {
        viewModelScope.launch { repository.sync() }
    }

    fun onSongClick(song: Song) {
        val songs = (uiState.value as? LibraryUiState.Content)?.songs ?: return
        val index = songs.indexOf(song)
        if (index >= 0) playerController.play(songs, index)
    }

    fun onShuffleClick() {
        val songs = (uiState.value as? LibraryUiState.Content)?.songs ?: return
        if (songs.isNotEmpty()) playerController.play(songs.shuffled(), 0)
    }
}
