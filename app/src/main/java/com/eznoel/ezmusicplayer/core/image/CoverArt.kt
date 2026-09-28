package com.eznoel.ezmusicplayer.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.size.pxOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

/** Lo que se le entrega a Coil como `model`. Es una data class, así que Coil detecta cuándo cambia. */
data class CoverArtRequest(
    val contentUri: String,
    val dateModifiedSec: Long,
)

class CoverArtKeyer : Keyer<CoverArtRequest> {
    override fun key(data: CoverArtRequest, options: Options): String =
        "cover:${data.contentUri}:${data.dateModifiedSec}"
}

class CoverArtFetcher(
    private val request: CoverArtRequest,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val context = options.context
        val uri = request.contentUri.toUri()
        // El tamaño lo dicta el composable que pide la imagen: nunca cargamos más píxeles de los que se dibujan.
        val width = options.size.width.pxOrElse { DEFAULT_PX }
        val height = options.size.height.pxOrElse { DEFAULT_PX }

        val bitmap = withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(width, height), null)
            } else {
                loadEmbeddedArt(context, uri, width, height)
            }
        }
        return ImageFetchResult(
            image = bitmap.asImage(),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
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

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, reqW, reqH)
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                ?: throw IOException("No se pudo decodificar la carátula")
        } finally {
            retriever.release() // AutoCloseable solo desde API 29, así que se libera a mano
        }
    }

    // Potencia de 2 más grande que aún deja la imagen igual o mayor que lo pedido.
    private fun sampleSizeFor(width: Int, height: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= reqW && height / (sample * 2) >= reqH) sample *= 2
        return sample
    }

    class Factory : Fetcher.Factory<CoverArtRequest> {
        override fun create(
            data: CoverArtRequest,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = CoverArtFetcher(data, options)
    }

    private companion object {
        const val DEFAULT_PX = 256
    }
}
