package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.eznoel.ezmusicplayer.core.image.CoverSeeds
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

/**
 * ColorScheme de la pantalla Now Playing derivado de la carátula, generado con HCT
 * (material-color-utilities vía MaterialKolor).
 *
 * Solo se sobrescriben los roles que la pantalla realmente usa; el resto queda del tema de la app.
 * Sin semillas (cargando o sin carátula) se usa el tema base. Los cambios se animan rol por rol.
 */
@Composable
internal fun rememberCoverColorScheme(seeds: CoverSeeds?): ColorScheme {
    val base = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()
    val target = remember(seeds, base, darkTheme) {
        seeds?.toColorScheme(darkTheme) ?: base
    }

    return base.copy(
        background = target.background.animated("background"),
        onBackground = target.onBackground.animated("onBackground"),
        surface = target.surface.animated("surface"),
        onSurface = target.onSurface.animated("onSurface"),
        onSurfaceVariant = target.onSurfaceVariant.animated("onSurfaceVariant"),
        surfaceContainer = target.surfaceContainer.animated("surfaceContainer"), // fondo del DropdownMenu
        primary = target.primary.animated("primary"),
        onPrimary = target.onPrimary.animated("onPrimary"),
        primaryContainer = target.primaryContainer.animated("primaryContainer"),
        onPrimaryContainer = target.onPrimaryContainer.animated("onPrimaryContainer"),
        secondaryContainer = target.secondaryContainer.animated("secondaryContainer"),
        onSecondaryContainer = target.onSecondaryContainer.animated("onSecondaryContainer"),
    )
}

@Composable
private fun Color.animated(label: String): Color =
    animateColorAsState(targetValue = this, animationSpec = tween(400), label = "cover_$label").value

/**
 * HCT hace todo el trabajo: PaletteStyle.Content conserva el matiz y la intensidad de la carátula
 * (alternativas: Fidelity, TonalSpot más apagado, Vibrant, Expressive) y los tonos por rol ya
 * garantizan contraste, tanto en modo claro como oscuro.
 */
private fun CoverSeeds.toColorScheme(dark: Boolean): ColorScheme {
    val generated = dynamicColorScheme(
        seedColor = primary,
        isDark = dark,
        secondary = secondary, // quítalo si prefieres que HCT derive el secundario del primario
        style = PaletteStyle.Content,
    )
    // Fondo de página levemente teñido (en vez del surface casi blanco/negro).
    val page = generated.surfaceContainer
    return generated.copy(
        background = page,
        surface = page,
        onBackground = generated.onSurface,
    )
}