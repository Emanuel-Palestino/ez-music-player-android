package com.eznoel.ezmusicplayer.feature.files.tagedit

import android.app.Activity
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.navigation.EditTagsRoute

@Composable
fun EditTagsScreen(
    route: EditTagsRoute,
    onClose: () -> Unit,
    viewModel: EditTagsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.retryAfterPermissionGranted(onSaved = onClose)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                viewModel.updateForm { it.copy(coverBytes = bytes, coverMimeType = "image/jpeg") }
            }
        }
    }

    when (val state = uiState) {
        is EditTagsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is EditTagsUiState.Content -> {
            EditTagsContent(
                state = state,
                onClose = onClose,
                onFieldChange = viewModel::updateForm,
                onPickCover = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onSaveClick = {
                    viewModel.onSaveClicked(
                        onNeedPermission = { sender ->
                            permissionLauncher.launch(IntentSenderRequest.Builder(sender).build())
                        },
                        onSaved = onClose
                    )
                }
            )
        }
    }
}

@Composable
private fun EditTagsContent(
    state: EditTagsUiState.Content,
    onClose: () -> Unit,
    onFieldChange: ((TagFormState) -> TagFormState) -> Unit,
    onPickCover: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "Cerrar") }
            Text("Edit tags", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = onSaveClick, enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp))
                else Text("Save")
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                val bitmap = remember(state.form.coverBytes) {
                    state.form.coverBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap, contentDescription = null,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                IconButton(onClick = onPickCover, modifier = Modifier.size(28.dp).offset(x = 4.dp, y = 4.dp)) {
                    Icon(Icons.Filled.Image, contentDescription = "Cambiar carátula")
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "/${state.relativePath}${state.fileName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        LabeledField("Title", state.form.title) { onFieldChange { form -> form.copy(title = it) } }
        Spacer(Modifier.height(12.dp))
        LabeledField("Artist", state.form.artist) { onFieldChange { form -> form.copy(artist = it) } }
        Spacer(Modifier.height(12.dp))
        LabeledField("Album", state.form.album) { onFieldChange { form -> form.copy(album = it) } }
        Spacer(Modifier.height(12.dp))
        LabeledField("Album artist", state.form.albumArtist) { onFieldChange { form -> form.copy(albumArtist = it) } }
        Spacer(Modifier.height(12.dp))
        Row {
            LabeledField("Track", state.form.trackNumber, modifier = Modifier.weight(1f)) {
                onFieldChange { form -> form.copy(trackNumber = it) }
            }
            Spacer(Modifier.width(8.dp))
            LabeledField("Year", state.form.year, modifier = Modifier.weight(1f)) {
                onFieldChange { form -> form.copy(year = it) }
            }
            Spacer(Modifier.width(8.dp))
            LabeledField("Genre", state.form.genre, modifier = Modifier.weight(1f)) {
                onFieldChange { form -> form.copy(genre = it) }
            }
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}