package com.eznoel.ezmusicplayer.data.library

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.room.withTransaction
import com.eznoel.ezmusicplayer.core.database.AppDatabase
import com.eznoel.ezmusicplayer.core.database.SongDao
import com.eznoel.ezmusicplayer.core.database.SongEntity
import com.eznoel.ezmusicplayer.core.permissions.MediaPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibrarySyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val songDao: SongDao,
    private val scanner: MediaStoreScanner,
) {
    private val mutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    suspend fun sync() = mutex.withLock {
        // Sin permiso, MediaStore devuelve un cursor vacío (no lanza excepción).
        // Sincronizar en ese estado marcaría TODA la biblioteca como no disponible.
        val granted = ContextCompat.checkSelfPermission(context, MediaPermission.permissionName) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) return@withLock

        _isSyncing.value = true
        try {
            val scanned = scanner.scan()
            val existingByMediaStoreId = songDao.getAll().associateBy { it.mediaStoreId }
            val scannedIds = HashSet<Long>(scanned.size)

            val toUpsert = ArrayList<SongEntity>()
            for (song in scanned) {
                scannedIds += song.mediaStoreId
                val existing = existingByMediaStoreId[song.mediaStoreId]
                val candidate = song.toEntity(existingId = existing?.id ?: 0L)
                // Si es nueva (existing == null) o cualquier campo difiere, se escribe.
                // Esto cubre también canciones que reaparecen (isAvailable pasa de false a true).
                if (candidate != existing) toUpsert += candidate
            }

            val gone = existingByMediaStoreId.values
                .filter { it.isAvailable && it.mediaStoreId !in scannedIds }
                .map { it.id }

            // Una sola transacción: la UI recibe un único cambio, no uno por lote.
            database.withTransaction {
                songDao.upsertAll(toUpsert)
                // SQLite en Android 9 limita a 999 variables por consulta.
                gone.chunked(500).forEach { songDao.markUnavailable(it) }
            }
        } finally {
            _isSyncing.value = false
        }
    }
}