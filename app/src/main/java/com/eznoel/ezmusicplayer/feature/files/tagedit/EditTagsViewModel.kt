package com.eznoel.ezmusicplayer.feature.files.tagedit

import android.content.IntentSender
import android.net.Uri
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

@HiltViewModel
class EditTagsViewModel @Inject constructor(
    private val repository: TagEditRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditTagsRoute>()
    private val uri = Uri.parse(route.uriString)

    private val _uiState = MutableStateFlow<EditTagsUiState>(EditTagsUiState.Loading)
    val uiState: StateFlow<EditTagsUiState> = _uiState.asStateFlow()

    private var pendingForm: TagFormState? = null

    init {
        viewModelScope.launch {
            val tag = repository.readTags(uri)
            _uiState.value = EditTagsUiState.Content(
                form = tag.toFormState(),
                fileName = route.fileName,
                relativePath = route.relativePath
            )
        }
    }

    fun updateForm(update: (TagFormState) -> TagFormState) {
        val current = _uiState.value as? EditTagsUiState.Content ?: return
        _uiState.value = current.copy(form = update(current.form))
    }

    fun onSaveClicked(onNeedPermission: (IntentSender) -> Unit, onSaved: () -> Unit) {
        val current = _uiState.value as? EditTagsUiState.Content ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null)
            when (val result = repository.writeTags(uri, current.form.toAudioTag())) {
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
            when (repository.writeTags(uri, form.toAudioTag())) {
                is TagWriteResult.Success -> onSaved()
                is TagWriteResult.Error -> { /* re-mostrar el error, mismo patrón de arriba */ }
                is TagWriteResult.NeedsPermission -> { /* no debería repetirse tras conceder */ }
            }
        }
    }
}