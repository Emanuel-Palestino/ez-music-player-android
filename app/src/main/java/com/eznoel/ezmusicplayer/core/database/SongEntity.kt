package com.eznoel.ezmusicplayer.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index("mediaStoreId", unique = true),
        Index("relativePath"),
        Index("dateAddedSec"),
        Index("artist"),
        Index("albumId"),
    ],
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    val displayName: String,
    val relativePath: String,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
    val isAvailable: Boolean = true,
)