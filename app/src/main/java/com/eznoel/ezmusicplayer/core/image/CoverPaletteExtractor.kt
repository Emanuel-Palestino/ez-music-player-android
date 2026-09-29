package com.eznoel.ezmusicplayer.core.image

import android.content.Context
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.eznoel.ezmusicplayer.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverPaletteExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Clave = contentUri + dateModifiedSec, mismo criterio que CoverArtKeyer,
    // para que una carátula editada no reutilice el color viejo.
    private val cache = LruCache<String, Color>(20)

    suspend fun extractColor(song: Song, fallback: Color): Color {
        val key = "${song.contentUri}:${song.dateModifiedSec}"
        cache.get(key)?.let { return it }

        val bitmap = CoverArtLoader.loadBitmap(context, song.contentUri, reqWidth = 128, reqHeight = 128)
            ?: return fallback

        val color = withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            // Vibrant primero (el más "vivo" de la imagen); si no existe, Muted es el respaldo
            // más seguro porque casi siempre hay algún tono apagado presente.
            val swatch = palette.vibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch
            swatch?.rgb?.let(::Color) ?: fallback
        }
        cache.put(key, color)
        return color
    }
}