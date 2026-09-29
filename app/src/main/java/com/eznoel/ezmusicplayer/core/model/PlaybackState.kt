package com.eznoel.ezmusicplayer.core.model

data class PlaybackState(
    val currentSong: Song?,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val durationMs: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
) {
    companion object {
        val Empty = PlaybackState(
            currentSong = null,
            isPlaying = false,
            isBuffering = false,
            durationMs = 0L,
            hasNext = false,
            hasPrevious = false,
        )
    }
}