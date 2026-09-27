package com.eznoel.ezmusicplayer.data.tagedit

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import com.tagkit.MediaTagEditor
import com.tagkit.model.AudioTag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagEditRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun readTags(uri: Uri): AudioTag = withContext(Dispatchers.IO) {
        val temp = copyToTemp(uri)
        try { MediaTagEditor.readTags(temp) } finally { temp.delete() }
    }

    suspend fun writeTags(uri: Uri, tag: AudioTag): TagWriteResult = withContext(Dispatchers.IO) {
        val temp = copyToTemp(uri)
        try {
            MediaTagEditor.writeTags(temp, tag)
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                temp.inputStream().use { input -> input.copyTo(output) }
            } ?: return@withContext TagWriteResult.Error("No se pudo abrir el archivo para escritura")
            TagWriteResult.Success
        } catch (e: RecoverableSecurityException) {
            TagWriteResult.NeedsPermission(e.userAction.actionIntent.intentSender)
        } catch (e: Exception) {
            TagWriteResult.Error(e.message ?: "Error desconocido al guardar")
        } finally {
            temp.delete()
        }
    }

    private fun copyToTemp(uri: Uri): File {
        val temp = File.createTempFile("tag_edit_", null, context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            temp.outputStream().use { output -> input.copyTo(output) }
        } ?: error("No se pudo leer el archivo: $uri")
        return temp
    }
}

sealed interface TagWriteResult {
    data object Success : TagWriteResult
    data class NeedsPermission(val intentSender: IntentSender) : TagWriteResult
    data class Error(val message: String) : TagWriteResult
}