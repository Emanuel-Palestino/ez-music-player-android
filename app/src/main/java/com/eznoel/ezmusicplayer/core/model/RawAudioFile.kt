package com.eznoel.ezmusicplayer.core.model

import android.net.Uri

data class RawAudioFile(
    val id: Long,
    val contentUri: Uri,
    val displayName: String,
    val relativePath: String,
    val durationMs: Long,
    val sizeBytes: Long
)