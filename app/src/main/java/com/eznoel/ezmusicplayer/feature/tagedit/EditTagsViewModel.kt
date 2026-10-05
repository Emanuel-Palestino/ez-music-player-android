package com.eznoel.ezmusicplayer.feature.tagedit

import android.content.IntentSender
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eznoel.ezmusicplayer.data.tagedit.TagEditRepository
import com.eznoel.ezmusicplayer.data.tagedit.TagWriteResult
import com.eznoel.ezmusicplayer.navigation.EditTagsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.eznoel.ezmusicplayer.data.library.LibraryRepository

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val tagRepository: TagEditRepository,
    private val libraryRepository: LibraryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditTagsRoute>()
    private val uri = route.uriString.toUri()

    private val _uiState = MutableStateFlow<EditTagsUiState>(EditTagsUiState.Loading)
    val uiState: StateFlow<EditTagsUiState> = _uiState.asStateFlow()

    private var pendingForm: TagFormState? = null

    init {
        viewModelScope.launch {
            // 1) Datos rápidos desde Room: pintan la pantalla de inmediato
            val song = libraryRepository.getSongById(route.songId)
            _uiState.value = EditTagsUiState.Content(
                song = song,
                form = TagFormState(),
                fileName = song?.displayName.orEmpty(),
                relativePath = song?.relativePath.orEmpty(),
                format = song?.format.orEmpty(),
                fileSizeBytes = song?.sizeBytes ?: 0,
                isLoadingTags = true
            )

            // 2) Etiquetas reales del archivo: sobrescriben el formulario
            runCatching { tagRepository.readTags(uri) }
                .onSuccess { tag ->
                    updateContent { it.copy(form = tag.toFormState(), isLoadingTags = false) }
                }
                .onFailure {
                    // Se deja isLoadingTags = true a propósito: guardar sin las etiquetas
                    // reales borraría albumArtist, genre y la carátula.
                    updateContent { it.copy(errorMessage = "No se pudieron leer las etiquetas") }
                }
        }
    }

    private fun updateContent(block: (EditTagsUiState.Content) -> EditTagsUiState.Content) {
        (_uiState.value as? EditTagsUiState.Content)?.let { _uiState.value = block(it) }
    }

    fun updateForm(update: (TagFormState) -> TagFormState) {
        val current = _uiState.value as? EditTagsUiState.Content ?: return
        if (current.isLoadingTags) return
        _uiState.value = current.copy(form = update(current.form))
    }

    fun onSaveClicked(onNeedPermission: (IntentSender) -> Unit, onSaved: () -> Unit) {
        val current = _uiState.value as? EditTagsUiState.Content ?: return
        if (current.isLoadingTags) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null)
            when (val result = tagRepository.writeTags(uri, current.form.toAudioTag())) {
                is TagWriteResult.Success -> onSaved()
                is TagWriteResult.NeedsPermission -> {
                    pendingForm = current.form
                    _uiState.value = current.copy(isSaving = false)
                    onNeedPermission(result.intentSender)
                }
                is TagWriteResult.Error -> {
                    _uiState.value = current.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    fun retryAfterPermissionGranted(onSaved: () -> Unit) {
        val form = pendingForm ?: return
        viewModelScope.launch {
            when (tagRepository.writeTags(uri, form.toAudioTag())) {
                is TagWriteResult.Success -> onSaved()
                is TagWriteResult.Error -> { /* re-mostrar el error, mismo patrón de arriba */ }
                is TagWriteResult.NeedsPermission -> { /* no debería repetirse tras conceder */ }
            }
        }
    }
}