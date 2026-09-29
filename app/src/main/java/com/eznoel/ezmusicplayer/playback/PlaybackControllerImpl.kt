package com.eznoel.ezmusicplayer.playback

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.eznoel.ezmusicplayer.core.model.PlaybackState
import com.eznoel.ezmusicplayer.core.model.Song
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlayerController {

    // Dispatchers.Main.immediate: MediaController exige que todas sus llamadas
    // ocurran en el hilo principal. Este scope garantiza eso sin que cada método
    // tenga que acordarse de saltar de hilo.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controller: MediaController? = null
    private var isConnecting = false
    private var currentQueue: List<Song> = emptyList()

    private val _state = MutableStateFlow(PlaybackState.Empty)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    override val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    init {
       connect()
    }

    // Se llama cada vez que la app vuelve a primer plano (ver MiniPlayerViewModel).
    // No hace nada si ya hay una conexión viva o un intento en curso.
    fun ensureConnected() {
        if (controller != null || isConnecting) return
        connect()
    }

    private fun connect() {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken)
            .setListener(object : MediaController.Listener {
                // Se dispara cuando el servicio libera la sesión (por ejemplo, en onTaskRemoved).
                override fun onDisconnected(controller: MediaController) {
                    this@PlayerControllerImpl.controller = null
                    _state.value = PlaybackState.Empty
                    _positionMs.value = 0L
                }
            })
            .buildAsync()

        future.addListener(
            {
                try {
                    controller = future.get().also { it.addListener(playerListener) }
                    startPositionTicker()
                } catch (e: Exception) {
                    // Pudo rechazar la conexión por una carrera con el cierre del servicio anterior.
                    // No reintenta sola: la próxima llamada a ensureConnected() lo hará.
                    Log.w(TAG, "No se pudo conectar al MediaController", e)
                    isConnecting = false
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = updateState()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updateState()
        override fun onPlaybackStateChanged(playbackState: Int) = updateState()
    }

    private fun updateState() {
        val c = controller ?: return
        val song = c.currentMediaItem?.mediaId?.toLongOrNull()
            ?.let { id -> currentQueue.find { it.id == id } }

        _state.value = PlaybackState(
            currentSong = song,
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            durationMs = c.duration.coerceAtLeast(0L),
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem(),
        )
    }

    private fun startPositionTicker() {
        scope.launch {
            while (isActive) {
                controller?.let { _positionMs.value = it.currentPosition }
                delay(200)
            }
        }
    }

    override fun play(songs: List<Song>, startIndex: Int) {
        currentQueue = songs
        controller?.apply {
            setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            prepare()
            play()
        }
    }

    override fun togglePlayPause() {
        controller?.apply { if (isPlaying) pause() else play() }
    }

    override fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    override fun skipNext() {
        controller?.seekToNext()
    }

    override fun skipPrevious() {
        controller?.seekToPrevious()
    }

    private companion object {
        const val TAG = "PlayerControllerImpl"
    }
}