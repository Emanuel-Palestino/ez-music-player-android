package com.eznoel.ezmusicplayer.core.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    val format: String,
    val contentUri: String,
    val displayName: String,
    val relativePath: String,
    val sizeBytes: Long,
    val dateAddedSec: Long,
)

data class LibrarySummary(
    val songCount: Int,
    val totalSizeBytes: Long,
)