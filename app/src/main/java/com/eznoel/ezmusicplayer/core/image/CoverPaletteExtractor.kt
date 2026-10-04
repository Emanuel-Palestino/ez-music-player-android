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

/**
 * Colores "semilla" extraídos de una carátula. Son crudos a propósito: la UI los convierte en roles
 * (fondo, primario, contenedores...) según el tema claro/oscuro, así el caché no depende del tema.
 *
 * @property primary tono más representativo de la imagen; define el esquema.
 * @property secondary tono apagado de la imagen; si es null, HCT lo deriva de [primary].
 */
data class CoverSeeds(
    val primary: Color,
    val secondary: Color? = null,
)

@Singleton
class CoverPaletteExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Clave = contentUri + dateModifiedSec, mismo criterio que CoverArtKeyer,
    // para que una carátula editada no reutilice el color viejo.
    private val cache = LruCache<String, CoverSeeds>(20)

    /** Devuelve las semillas de la carátula, o null si la canción no tiene carátula o no se pudo leer. */
    suspend fun extractSeeds(song: Song): CoverSeeds? {
        val key = "${song.contentUri}:${song.dateModifiedSec}"
        cache.get(key)?.let { return it }

        val bitmap = CoverArtLoader.loadBitmap(context, song.contentUri, reqWidth = 128, reqHeight = 128)
            ?: return null

        val seeds = withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            // Vibrant primero (el más "vivo" de la imagen); si no existe, Muted es el respaldo
            // más seguro porque casi siempre hay algún tono apagado presente.
            val primary = palette.vibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch
            ?: return@withContext null
            val secondary = palette.mutedSwatch ?: palette.darkMutedSwatch ?: primary
            CoverSeeds(primary = Color(primary.rgb), secondary = Color(secondary.rgb))
        }
        if (seeds != null) cache.put(key, seeds)
        return seeds
    }

    /** Se conserva por compatibilidad con otros usos: solo el color principal. */
    suspend fun extractColor(song: Song, fallback: Color): Color =
        extractSeeds(song)?.primary ?: fallback
}