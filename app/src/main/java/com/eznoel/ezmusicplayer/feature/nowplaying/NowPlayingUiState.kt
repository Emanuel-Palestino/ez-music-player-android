package com.eznoel.ezmusicplayer.feature.nowplaying

import com.eznoel.ezmusicplayer.core.image.CoverSeeds
import com.eznoel.ezmusicplayer.core.model.RepeatMode
import com.eznoel.ezmusicplayer.core.model.Song

data class NowPlayingUiState(
    val song: Song,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val isShuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
    val queueSize: Int,
    val coverSeeds: CoverSeeds?,
)