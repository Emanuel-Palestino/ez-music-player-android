package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.core.image.CoverPaletteExtractor
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
    private val paletteExtractor: CoverPaletteExtractor,
) : ViewModel() {

    private val backgroundColor = MutableStateFlow<Color?>(null)
    private var lastSongId: Long? = null

    val uiState: StateFlow<NowPlayingUiState?> = combine(
        playerController.state,
        playerController.positionMs,
        backgroundColor,
    ) { state, position, color ->
        state.toUiState(position, color)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = playerController.state.value.toUiState(playerController.positionMs.value, null),
    )

    init {
        // Solo se dispara cuando cambia la canción, no en cada tick de posición.
        viewModelScope.launch {
            playerController.state
                .map { it.currentSong }
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collect { song ->
                    lastSongId = song?.id
                    backgroundColor.value = null // limpia mientras se calcula la nueva
                    if (song != null) {
                        val fallback = Color(0xFFE8DEF8) // valor neutro si falla la extracción
                        val color = paletteExtractor.extractColor(song, fallback)
                        // Evita pintar un color viejo si el usuario ya saltó a otra canción
                        // mientras se calculaba este (extractColor es async).
                        if (lastSongId == song.id) backgroundColor.value = color
                    }
                }
        }
    }

    fun onPlayPauseClick() = playerController.togglePlayPause()
    fun onNextClick() = playerController.skipNext()
    fun onPreviousClick() = playerController.skipPrevious()
    fun onShuffleClick() = playerController.toggleShuffle()
    fun onRepeatClick() = playerController.cycleRepeatMode()
    fun onSeek(positionMs: Long) = playerController.seekTo(positionMs)
}


private fun PlaybackState.toUiState(positionMs: Long, backgroundColor: Color?): NowPlayingUiState? {
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
        backgroundColor = backgroundColor,
    )
}