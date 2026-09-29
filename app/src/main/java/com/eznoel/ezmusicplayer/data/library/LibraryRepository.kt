package com.eznoel.ezmusicplayer.data.library

import com.eznoel.ezmusicplayer.core.model.LibraryFolder
import com.eznoel.ezmusicplayer.core.model.LibrarySummary
import com.eznoel.ezmusicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LibraryRepository {
    val isSyncing: StateFlow<Boolean>
    fun observeSongs(): Flow<List<Song>>
    fun observeSummary(): Flow<LibrarySummary>
    fun observeFolders(): Flow<List<LibraryFolder>>
    suspend fun setFolderIncluded(relativePath: String, included: Boolean)
    suspend fun sync()
    suspend fun forceRescan()
    suspend fun getSongById(id: Long): Song?
}