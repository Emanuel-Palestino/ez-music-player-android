package com.eznoel.ezmusicplayer.data.rawfiles

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.folderPrefsDataStore by preferencesDataStore(name = "folder_prefs")

@Singleton
class FolderPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }

    val excludedFolders: Flow<Set<String>> = context.folderPrefsDataStore.data
        .map { prefs -> prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet() }

    suspend fun setFolderExcluded(relativePath: String, excluded: Boolean) {
        context.folderPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
            prefs[Keys.EXCLUDED_FOLDERS] = if (excluded) current + relativePath else current - relativePath
        }
    }
}