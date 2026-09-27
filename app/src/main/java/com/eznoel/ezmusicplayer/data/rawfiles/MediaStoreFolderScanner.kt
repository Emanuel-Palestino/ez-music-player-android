package com.eznoel.ezmusicplayer.data.rawfiles

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

data class DiscoveredFolder(
    val relativePath: String,
    val trackCount: Int,
    val totalSizeBytes: Long,
)

class MediaStoreFolderScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun discoverFolders(): List<DiscoveredFolder> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.SIZE,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val accumulators = mutableMapOf<String, FolderAccumulator>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null,
        )?.use { cursor ->
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            while (cursor.moveToNext()) {
                val path = cursor.getString(pathCol) ?: continue
                val size = cursor.getLong(sizeCol)
                val acc = accumulators.getOrPut(path) { FolderAccumulator() }
                acc.trackCount++
                acc.totalSizeBytes += size
            }
        }

        accumulators.map { (path, acc) ->
            DiscoveredFolder(path, acc.trackCount, acc.totalSizeBytes)
        }.sortedBy { it.relativePath }
    }

    // only covers the primary volume, is missing to iterate MediaStore.getExternalVolumeNames()
    suspend fun forceRescan() = suspendCancellableCoroutine<Unit> { cont ->
        val rootPath = Environment.getExternalStorageDirectory().path
        MediaScannerConnection.scanFile(context, arrayOf(rootPath), null) {_, _ ->
            if (cont.isActive) cont.resume(Unit)
        }
    }

    private class FolderAccumulator {
        var trackCount = 0
        var totalSizeBytes = 0L
    }
}