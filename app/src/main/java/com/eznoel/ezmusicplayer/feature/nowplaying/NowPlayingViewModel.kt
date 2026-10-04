package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eznoel.ezmusicplayer.core.image.CoverPaletteExtractor
import com.eznoel.ezmusicplayer.core.image.CoverSeeds
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

    private val coverSeeds = MutableStateFlow<CoverSeeds?>(null)
    private var lastSongId: Long? = null

    val uiState: StateFlow<NowPlayingUiState?> = combine(
        playerController.state,
        playerController.positionMs,
        coverSeeds,
    ) { state, position, seeds ->
        state.toUiState(position, seeds)
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
                    if (song == null) {
                        coverSeeds.value = null
                        return@collect
                    }
                    // No se limpia antes de calcular: así los colores pasan de una canción a la
                    // siguiente con una sola animación, sin volver al tema base en medio.
                    val seeds = paletteExtractor.extractSeeds(song) // null si no hay carátula o falla
                    // Evita pintar colores viejos si el usuario ya saltó a otra canción
                    // mientras se calculaba este (extractSeeds es async).
                    if (lastSongId == song.id) coverSeeds.value = seeds
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