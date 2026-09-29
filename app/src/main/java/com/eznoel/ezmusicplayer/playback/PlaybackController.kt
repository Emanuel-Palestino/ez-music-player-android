package com.eznoel.ezmusicplayer.playback

import com.eznoel.ezmusicplayer.core.model.PlaybackState
import com.eznoel.ezmusicplayer.core.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlaybackState>
    val positionMs: StateFlow<Long>

    fun play(songs: List<Song>, startIndex: Int)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
}