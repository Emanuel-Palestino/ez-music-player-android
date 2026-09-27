package com.eznoel.ezmusicplayer.data.rawfiles

import com.eznoel.ezmusicplayer.core.model.LibraryFolder
import com.eznoel.ezmusicplayer.core.model.RawAudioFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface RawFilesRepository {
    fun observeFolders(): Flow<List<LibraryFolder>>
    fun observeFiles(): Flow<List<RawAudioFile>>
    suspend fun refreshFolders()
    suspend fun refreshFiles()
    suspend fun setFolderIncluded(relativePath: String, included: Boolean)
    suspend fun forceRescan()
}

@Singleton
class RawFilesRepositoryImpl @Inject constructor(
    private val folderScanner: MediaStoreFolderScanner,
    private val fileScanner: MediaStoreFileScanner,
    private val prefsRepo: FolderPreferencesRepository
) : RawFilesRepository {

    private val discoveredFolders = MutableStateFlow<List<DiscoveredFolder>>(emptyList())
    private val discoveredFiles = MutableStateFlow<List<RawAudioFile>>(emptyList())

    override fun observeFolders(): Flow<List<LibraryFolder>> =
        combine(discoveredFolders, prefsRepo.excludedFolders) { folders, excluded ->
            folders.map { folder ->
                LibraryFolder(
                    relativePath = folder.relativePath,
                    trackCount = folder.trackCount,
                    totalSizeBytes = folder.totalSizeBytes,
                    isIncluded = folder.relativePath !in excluded
                )
            }
        }

    override fun observeFiles(): Flow<List<RawAudioFile>> =
        combine(discoveredFiles, prefsRepo.excludedFolders) { files, excluded ->
            // Possible bug with nested folders
            files.filter { it.relativePath !in excluded }
        }

    override suspend fun refreshFolders() {
        discoveredFolders.value = folderScanner.discoverFolders()
    }

    override suspend fun refreshFiles() {
        discoveredFiles.value = fileScanner.scanFiles()
    }

    override suspend fun setFolderIncluded(relativePath: String, included: Boolean) {
        prefsRepo.setFolderExcluded(relativePath, excluded = !included)
    }

    override suspend fun forceRescan() {
        folderScanner.forceRescan()
        refreshFolders()
        refreshFiles()
    }
}