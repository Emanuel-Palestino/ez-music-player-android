package com.eznoel.ezmusicplayer.playback

import android.content.Intent
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.eznoel.ezmusicplayer.data.library.LibraryRepository
import com.eznoel.ezmusicplayer.data.playback.PersistedQueue
import com.eznoel.ezmusicplayer.data.playback.PlaybackStateStore
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var libraryRepository: LibraryRepository
    @Inject lateinit var playbackStateStore: PlaybackStateStore

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(PersistenceListener())
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .build()

        serviceScope.launch { restoreQueueIfAny(player) }
    }

    private suspend fun restoreQueueIfAny(player: ExoPlayer) {
        val saved = playbackStateStore.load() ?: return
        val songs = saved.songIds.mapNotNull { libraryRepository.getSongById(it) }
        if (songs.isEmpty()) return

        val startIndex = saved.currentIndex.coerceIn(0, songs.lastIndex)
        withContext(Dispatchers.Main) {
            player.setMediaItems(songs.map { it.toMediaItem() }, startIndex, saved.positionMs)
            player.prepare()
            // playWhenReady queda en false por defecto: se restaura en pausa, no sonando solo.
        }
    }

    // Guarda en cada pausa/cambio de pista, y cada 5s mientras suena, para no perder
    // más de unos segundos de posición si el sistema mata el proceso de golpe.
    private inner class PersistenceListener : Player.Listener {
        // Hilo principal: aquí sí se puede leer mediaSession.player con seguridad.
        private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var tickerJob: Job? = null

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            persistNow()
            tickerJob?.cancel()
            if (isPlaying) {
                tickerJob = mainScope.launch {   // antes: serviceScope (IO) — causaba el crash
                    while (isActive) {
                        delay(5_000)
                        persistNow()
                    }
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = persistNow()

        override fun onPlayerError(error: PlaybackException) {
            Log.w(TAG, "Error de reproducción: ${error.errorCodeName}", error)
        }

        // Se llama siempre desde el hilo principal (callbacks del Player, o el ticker de arriba).
        private fun persistNow() {
            val session = mediaSession ?: return
            val player = session.player
            if (player.mediaItemCount == 0) return
            val ids = (0 until player.mediaItemCount).mapNotNull {
                player.getMediaItemAt(it).mediaId.toLongOrNull()
            }
            val state = PersistedQueue(
                songIds = ids,
                currentIndex = player.currentMediaItemIndex,
                positionMs = player.currentPosition,
            )
            // Esto sí puede ir a IO: ya no toca el player, solo escribe el snapshot ya leído.
            serviceScope.launch { playbackStateStore.save(state) }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // Detiene todo al cerrar la app desde Recientes, sonando o no. No toca lo persistido:
    // el punto guardado debe sobrevivir para restaurarse la próxima vez que se abra la app.
    override fun onTaskRemoved(rootIntent: Intent?) {
        persistCurrentStateBlocking()
        mediaSession?.player?.apply { stop(); release() }
        mediaSession?.release()
        mediaSession = null
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    // Guardado síncrono porque el proceso puede morir apenas termine este método;
    // un launch() normal aquí podría no alcanzar a ejecutarse.
    private fun persistCurrentStateBlocking() {
        val player = mediaSession?.player ?: return
        if (player.mediaItemCount == 0) return
        val ids = (0 until player.mediaItemCount).mapNotNull {
            player.getMediaItemAt(it).mediaId.toLongOrNull()
        }
        val state = PersistedQueue(
            songIds = ids,
            currentIndex = player.currentMediaItemIndex,
            positionMs = player.currentPosition,
        )
        runBlocking { playbackStateStore.save(state) }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class SessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> = CoroutineScope(Dispatchers.IO).future {
            mediaItems.mapNotNullTo(mutableListOf()) { item ->
                val songId = item.mediaId.toLongOrNull() ?: return@mapNotNullTo null
                libraryRepository.getSongById(songId)?.toMediaItem()
            }
        }
    }

    private companion object {
        const val TAG = "PlaybackService"
    }
}