package com.eznoel.ezmusicplayer.feature.miniplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.playback.PlayerControllerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playerController: PlayerControllerImpl,
) : ViewModel() {

    init {
        playerController.ensureConnected()
    }

    val uiState: StateFlow<MiniPlayerUiState?> = combine(
        playerController.state,
        playerController.positionMs,
    ) { state, position ->
        val song = state.currentSong ?: return@combine null
        MiniPlayerUiState(
            song = song,
            isPlaying = state.isPlaying,
            progress = if (state.durationMs > 0) {
                (position.toFloat() / state.durationMs).coerceIn(0f, 1f)
            } else 0f,
            hasNext = state.hasNext,
            hasPrevious = state.hasPrevious,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun onPlayPauseClick() = playerController.togglePlayPause()

    fun onNextClick() = playerController.skipNext()

    fun onPreviousClick() = playerController.skipPrevious()
}