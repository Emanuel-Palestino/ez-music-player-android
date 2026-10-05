package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.core.image.CoverPaletteExtractor
import com.eznoel.ezmusicplayer.core.image.CoverSeeds
import com.eznoel.ezmusicplayer.core.image.CurrentCoverSeeds
import com.eznoel.ezmusicplayer.core.model.PlaybackState
import com.eznoel.ezmusicplayer.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val currentCoverSeeds: CurrentCoverSeeds,
) : ViewModel() {

    val uiState: StateFlow<NowPlayingUiState?> = combine(
        playerController.state,
        playerController.positionMs,
        currentCoverSeeds.seeds,
    ) { state, position, seeds ->
        state.toUiState(position, seeds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        // Ya con las semillas: el primer frame sale con los colores correctos.
        initialValue = playerController.state.value.toUiState(
            playerController.positionMs.value,
            currentCoverSeeds.seeds.value,
        ),
    )

    fun onPlayPauseClick() = playerController.togglePlayPause()
    fun onNextClick() = playerController.skipNext()
    fun onPreviousClick() = playerController.skipPrevious()
    fun onShuffleClick() = playerController.toggleShuffle()
    fun onRepeatClick() = playerController.cycleRepeatMode()
    fun onSeek(positionMs: Long) = playerController.seekTo(positionMs)
}


private fun PlaybackState.toUiState(positionMs: Long, coverSeeds: CoverSeeds?): NowPlayingUiState? {
    val song = currentSong ?: return null
    return NowPlayingUiState(
        song = song,
        isPlaying = isPlaying,
        positionMs = positionMs,
        durationMs = durationMs,
        hasNext = hasNext,
        hasPrevious = hasPrevious,
        isShuffleEnabled = isShuffleEnabled,
        repeatMode = repeatMode,
        queueSize = queueSize,
        coverSeeds = coverSeeds,
    )
}