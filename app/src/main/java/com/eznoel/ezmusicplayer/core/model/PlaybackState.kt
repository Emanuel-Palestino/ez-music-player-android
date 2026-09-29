package com.eznoel.ezmusicplayer.core.model

enum class RepeatMode { OFF, ALL, ONE }

data class PlaybackState(
    val currentSong: Song?,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val durationMs: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val isShuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
    val queueSize: Int, // Left songs after the current one
) {
    companion object {
        val Empty = PlaybackState(
            currentSong = null,
            isPlaying = false,
            isBuffering = false,
            durationMs = 0L,
            hasNext = false,
            hasPrevious = false,
            isShuffleEnabled = false,
            repeatMode = RepeatMode.OFF,
            queueSize = 0,
        )
    }
}