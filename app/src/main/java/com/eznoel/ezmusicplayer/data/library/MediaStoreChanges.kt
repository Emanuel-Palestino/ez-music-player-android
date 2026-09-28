package com.eznoel.ezmusicplayer.data.library

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreChanges @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Emite una señal cada vez que MediaStore avisa de un cambio en el audio. */
    val events: Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, // notifyForDescendants: avisa también por cambios en filas individuales
            observer,
        )
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.buffer(Channel.CONFLATED)
}