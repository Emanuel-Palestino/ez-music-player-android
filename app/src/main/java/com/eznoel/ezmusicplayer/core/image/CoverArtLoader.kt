package com.eznoel.ezmusicplayer.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

object CoverArtLoader {
    // Por debajo de este umbral, la miniatura cacheada del sistema es suficiente y más rápida.
    // Por encima (portadas grandes, Now Playing), se decodifica el archivo original.
    private const val THUMBNAIL_THRESHOLD_PX = 300

    suspend fun loadBitmap(context: Context, contentUri: String, reqWidth: Int, reqHeight: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val uri = contentUri.toUri()
            val useSystemThumbnail = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    maxOf(reqWidth, reqHeight) <= THUMBNAIL_THRESHOLD_PX

            runCatching {
                if (useSystemThumbnail) {
                    context.contentResolver.loadThumbnail(uri, Size(reqWidth, reqHeight), null)
                } else {
                    loadEmbeddedArt(context, uri, reqWidth, reqHeight)
                }
            }.getOrNull()
        }

    // API 28: sin loadThumbnail, se lee la imagen embebida y se decodifica reducida.
    private fun loadEmbeddedArt(context: Context, uri: Uri, reqW: Int, reqH: Int): Bitmap {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val bytes = retriever.embeddedPicture
                ?: throw FileNotFoundException("La canción no tiene carátula embebida")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, reqW, reqH)
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                ?: throw IOException("No se pudo decodificar la carátula")
        } finally {
            retriever.release() // AutoCloseable solo desde API 29, así que se libera a mano
        }
    }

    private fun sampleSizeFor(width: Int, height: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= reqW && height / (sample * 2) >= reqH) sample *= 2
        return sample
    }
}