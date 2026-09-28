package com.eznoel.ezmusicplayer.data.library

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class ScannedSong(
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
)

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    suspend fun scan(): List<ScannedSong> = withContext(Dispatchers.IO) {
        val hasRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val pathColumn = if (hasRelativePath) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            pathColumn,
        )
        val result = ArrayList<ScannedSong>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null,
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val modifiedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val pathCol = c.getColumnIndexOrThrow(pathColumn)

            while (c.moveToNext()) {
                val displayName = c.getString(nameCol).orEmpty()
                val rawPath = c.getString(pathCol).orEmpty()
                result += ScannedSong(
                    mediaStoreId = c.getLong(idCol),
                    title = c.getString(titleCol).takeUnless { it.isNullOrBlank() }
                        ?: displayName.substringBeforeLast('.'),
                    artist = c.getString(artistCol).cleanUnknown(),
                    album = c.getString(albumCol).cleanUnknown(),
                    albumId = c.getLong(albumIdCol),
                    durationMs = c.getLong(durationCol),
                    // Algunos archivos codifican disco*1000 + pista (1003 = disco 1, pista 3).
                    trackNumber = c.getInt(trackCol) % 1000,
                    year = c.getInt(yearCol),
                    displayName = displayName,
                    relativePath = if (hasRelativePath) rawPath else relativePathFromData(rawPath),
                    sizeBytes = c.getLong(sizeCol),
                    dateAddedSec = c.getLong(addedCol),
                    dateModifiedSec = c.getLong(modifiedCol),
                )
            }
        }
        result
    }

    /** Pide al sistema que reindexe el almacenamiento primario. */
    suspend fun forceRescan() = suspendCancellableCoroutine { cont ->
        val root = Environment.getExternalStorageDirectory().path
        MediaScannerConnection.scanFile(context, arrayOf(root), null) { _, _ ->
            if (cont.isActive) cont.resume(Unit)
        }
    }

    // MediaStore usa el literal "<unknown>" cuando falta el dato. Guardamos "" y que la UI decida el texto.
    private fun String?.cleanUnknown(): String =
        if (this == null || this == MediaStore.UNKNOWN_STRING) "" else this

    // API 28: "/storage/emulated/0/Music/Jazz/x.flac" -> "Music/Jazz/" (mismo formato que RELATIVE_PATH).
    private fun relativePathFromData(data: String): String {
        val withoutRoot = data.replaceFirst(Regex("^/storage/(emulated/\\d+|[^/]+)/"), "")
        val dir = withoutRoot.substringBeforeLast('/', "")
        return if (dir.isEmpty()) "" else "$dir/"
    }
}