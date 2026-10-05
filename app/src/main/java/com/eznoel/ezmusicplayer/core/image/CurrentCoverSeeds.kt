package com.eznoel.ezmusicplayer.core.image

import com.eznoel.ezmusicplayer.playback.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Semillas de color de la canción que suena ahora. Vive a nivel de app, así que se calcula una sola
 * vez por canción y tanto el MiniPlayer como Now Playing leen el mismo valor ya listo.
 */
@Singleton
class CurrentCoverSeeds @Inject constructor(
    playerController: PlayerController,
    extractor: CoverPaletteExtractor,
) {
    // Si ya tienes un @ApplicationScope, inyéctalo en lugar de crear este.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val seeds: StateFlow<CoverSeeds?> = playerController.state
        .map { it.currentSong }
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        // mapLatest cancela el cálculo anterior si el usuario salta de canción,
        // y reemplaza el chequeo manual de lastSongId.
        // No se emite null antes de calcular: el valor previo se mantiene hasta que llega el nuevo,
        // así los colores hacen una sola transición entre canciones.
        .mapLatest { song -> song?.let { extractor.extractSeeds(it) } }
        .stateIn(scope, SharingStarted.Eagerly, null)
}
