package com.eznoel.ezmusicplayer.data.playback

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playbackStateDataStore by preferencesDataStore(name = "playback_state")

@Singleton
class PlaybackStateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val QUEUE_JSON = stringPreferencesKey("queue_json")
    }

    suspend fun save(state: PersistedQueue) {
        context.playbackStateDataStore.edit { it[Keys.QUEUE_JSON] = Json.encodeToString(state) }
    }

    suspend fun load(): PersistedQueue? {
        val json = context.playbackStateDataStore.data.first()[Keys.QUEUE_JSON] ?: return null
        return runCatching { Json.decodeFromString<PersistedQueue>(json) }.getOrNull()
    }
}