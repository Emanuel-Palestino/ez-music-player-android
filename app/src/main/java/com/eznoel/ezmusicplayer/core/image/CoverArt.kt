package com.eznoel.ezmusicplayer.core.image


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
        // El tamaño lo dicta el composable que pide la imagen: nunca cargamos más píxeles de los que se dibujan.
        val width = options.size.width.pxOrElse { DEFAULT_PX }
        val height = options.size.height.pxOrElse { DEFAULT_PX }

        val bitmap = CoverArtLoader.loadBitmap(context, request.contentUri, width, height)
            ?: throw FileNotFoundException("La canción no tiene carátula")

        return ImageFetchResult(
            image = bitmap.asImage(),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
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
