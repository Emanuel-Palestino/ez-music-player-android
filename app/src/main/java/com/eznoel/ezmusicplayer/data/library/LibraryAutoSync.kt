package com.eznoel.ezmusicplayer.data.library

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class LibraryAutoSync @Inject constructor(
    private val changes: MediaStoreChanges,
    // Lazy: la base de datos y el escáner solo se construyen cuando llega el primer aviso,
    // así el arranque de la app no paga nada por esto.
    private val syncer: dagger.Lazy<LibrarySyncer>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    @OptIn(FlowPreview::class)
    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            changes.events
                .debounce(DEBOUNCE_MS)
                .collect {
                    Log.d(TAG, "Cambio detectado en MediaStore, sincronizando")
                    try {
                        syncer.get().sync()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Un fallo puntual no debe matar la escucha para siempre.
                        Log.w(TAG, "Falló la sincronización automática", e)
                    }
                }
        }
    }

    private companion object {
        const val TAG = "LibraryAutoSync"
        const val DEBOUNCE_MS = 3_000L
    }
}