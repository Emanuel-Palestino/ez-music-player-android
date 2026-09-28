package com.eznoel.ezmusicplayer.data.library

import com.eznoel.ezmusicplayer.core.database.SongDao
import com.eznoel.ezmusicplayer.core.model.LibraryFolder
import com.eznoel.ezmusicplayer.core.model.LibrarySummary
import com.eznoel.ezmusicplayer.core.model.Song
import com.eznoel.ezmusicplayer.data.rawfiles.FolderPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val syncer: LibrarySyncer,
    private val scanner: MediaStoreScanner,
    private val prefs: FolderPreferencesRepository,
) : LibraryRepository {

    override val isSyncing: StateFlow<Boolean> = syncer.isSyncing

    // flatMapLatest: cada vez que cambian las carpetas excluidas, Room re-ejecuta
    // la consulta con la nueva lista. Alternar un switch actualiza la Library al instante.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSongs(): Flow<List<Song>> =
        prefs.excludedFolders
            .flatMapLatest { excluded -> songDao.observeSongs(excluded.toList()) }
            .map { entities -> entities.map { it.toSong() } }
            .flowOn(Dispatchers.Default) // mapear miles de filas no debe ocurrir en el hilo principal

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSummary(): Flow<LibrarySummary> =
        prefs.excludedFolders
            .flatMapLatest { excluded -> songDao.observeStats(excluded.toList()) }
            .map { LibrarySummary(it.songCount, it.totalSizeBytes) }

    override fun observeFolders(): Flow<List<LibraryFolder>> =
        combine(songDao.observeFolders(), prefs.excludedFolders) { rows, excluded ->
            rows.map { row ->
                LibraryFolder(
                    relativePath = row.relativePath,
                    trackCount = row.trackCount,
                    totalSizeBytes = row.totalSizeBytes,
                    isIncluded = row.relativePath !in excluded,
                )
            }
        }

    override suspend fun setFolderIncluded(relativePath: String, included: Boolean) {
        prefs.setFolderExcluded(relativePath, excluded = !included)
    }

    override suspend fun sync() = syncer.sync()

    override suspend fun forceRescan() {
        scanner.forceRescan()
        syncer.sync()
    }
}