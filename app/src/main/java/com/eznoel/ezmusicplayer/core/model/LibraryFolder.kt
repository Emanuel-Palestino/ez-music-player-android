package com.eznoel.ezmusicplayer.core.model

data class LibraryFolder(
    val relativePath: String,
    val trackCount: Int,
    val totalSizeBytes: Long,
    val isIncluded: Boolean,
)