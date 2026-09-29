package com.eznoel.ezmusicplayer.data.playback

import kotlinx.serialization.Serializable

@Serializable
data class PersistedQueue(
    val songIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long,
)