package com.eznoel.ezmusicplayer.feature.miniplayer

import com.eznoel.ezmusicplayer.core.model.Song

data class MiniPlayerUiState(
    val song: Song,
    val isPlaying: Boolean,
    val progress: Float,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)