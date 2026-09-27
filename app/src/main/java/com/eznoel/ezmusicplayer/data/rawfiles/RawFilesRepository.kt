package com.eznoel.ezmusicplayer.data.rawfiles

import com.eznoel.ezmusicplayer.core.model.LibraryFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface RawFilesRepository {
    fun observeFolders(): Flow<List<LibraryFolder>>
    suspend fun refreshFolders()
    suspend fun setFolderIncluded(relativePath: String, included: Boolean)
    suspend fun forceRescan()
}

@Singleton
class RawFilesRepositoryImpl @Inject constructor(
    private val scanner: MediaStoreFolderScanner,
    private val prefsRepo: FolderPreferencesRepository
) : RawFilesRepository {

    private val discovered = MutableStateFlow<List<DiscoveredFolder>>(emptyList())

    override fun observeFolders(): Flow<List<LibraryFolder>> =
        combine(discovered, prefsRepo.excludedFolders) { folders, excluded ->
            folders.map { folder ->
                LibraryFolder(
                    relativePath = folder.relativePath,
                    trackCount = folder.trackCount,
                    totalSizeBytes = folder.totalSizeBytes,
                    isIncluded = folder.relativePath !in excluded
                )
            }
        }

    override suspend fun refreshFolders() {
        discovered.value = scanner.discoverFolders()
    }

    override suspend fun setFolderIncluded(relativePath: String, included: Boolean) {
        prefsRepo.setFolderExcluded(relativePath, excluded = !included)
    }

    override suspend fun forceRescan() {
        scanner.forceRescan()
        refreshFolders()
    }
}