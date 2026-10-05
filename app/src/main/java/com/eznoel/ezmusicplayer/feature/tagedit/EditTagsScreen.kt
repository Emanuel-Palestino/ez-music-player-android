package com.eznoel.ezmusicplayer.feature.tagedit

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.core.common.formatSize
import com.eznoel.ezmusicplayer.core.designsystem.SongCover
import com.eznoel.ezmusicplayer.core.model.Song

private val FieldShape = RoundedCornerShape(16.dp)

private val GENRES = listOf(
    "Alternative", "Blues", "Classical", "Country", "Electronic", "Folk",
    "Funk", "Hip-Hop", "Jazz", "Metal", "Pop", "Punk", "R&B", "Reggae",
    "Rock", "Soul", "Soundtrack"
)

@Composable
fun EditTagsScreen(
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
                viewModel.updateForm { it.copy(coverBytes = bytes, coverMimeType = "image/jpeg", coverChanged = true) }
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
    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    title = { Text("Editar Información") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        Button(
                            onClick = onSaveClick,
                            enabled = !state.isSaving && !state.isLoadingTags,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp))
                            else Text("Actualizar")
                        }
                    },
                )

                Box(Modifier.fillMaxWidth().height(4.dp)) {
                    if (state.isLoadingTags && state.errorMessage == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }

        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoverPicker(
                    song = state.song,
                    newCoverBytes = state.form.coverBytes,
                    enabled = !state.isLoadingTags,
                    onPickCover = onPickCover
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "/${state.relativePath}${state.fileName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.format.takeIf { it.isNotEmpty() }?.let { MetaChip(it.uppercase()) }
                        state.fileSizeBytes.let { MetaChip(formatSize(it)) }
                    }
                }
            }

            key(state.isLoadingTags) {
                Spacer(Modifier.height(20.dp))
                LabeledField(
                    "Título",
                    state.form.title,
                    !state.isLoadingTags
                ) { onFieldChange { form -> form.copy(title = it) } }

                Spacer(Modifier.height(12.dp))
                LabeledField(
                    "Artista",
                    state.form.artist,
                    !state.isLoadingTags
                ) { onFieldChange { form -> form.copy(artist = it) } }

                Spacer(Modifier.height(12.dp))
                LabeledField(
                    "Álbum",
                    state.form.album,
                    !state.isLoadingTags
                ) { onFieldChange { form -> form.copy(album = it) } }

                Spacer(Modifier.height(12.dp))
                LabeledField(
                    "Artista del Álbum",
                    state.form.albumArtist,
                    !state.isLoadingTags
                ) { onFieldChange { form -> form.copy(albumArtist = it) } }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledField(
                        "Pista",
                        state.form.trackNumber,
                        !state.isLoadingTags,
                        modifier = Modifier.weight(1f)
                    ) {
                        onFieldChange { form -> form.copy(trackNumber = it) }
                    }
                    LabeledField(
                        "Año", state.form.year,
                        !state.isLoadingTags,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    ) {
                        onFieldChange { form -> form.copy(year = it) }
                    }
                }

                Spacer(Modifier.height(12.dp))
                GenreDropdownField(
                    value = state.form.genre,
                    !state.isLoadingTags,
                ) {
                    onFieldChange { form -> form.copy(genre = it) }
                }
            }

            state.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CoverPicker(
    song: Song?,
    newCoverBytes: ByteArray?,
    enabled: Boolean,
    onPickCover: () -> Unit
) {
    val coverShape = RoundedCornerShape(24.dp)

    Box(Modifier.size(96.dp)) {
        if (song != null) {
            SongCover(
                song = song,
                shape = coverShape,
                crossFade = false,
                coverOverride = newCoverBytes,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(coverShape) // antes del clickable para que el ripple quede redondeado
                    .clickable(onClick = onPickCover, enabled = enabled)
            )
        }

        Surface(
            onClick = onPickCover,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.background),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Cambiar carátula",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = FieldShape,
        keyboardOptions = keyboardOptions
    )
}

/** Campo de género con flecha de desplegable. Sigue siendo editable para géneros personalizados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenreDropdownField(
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            enabled = enabled,
            onValueChange = onValueChange,
            label = { Text("Género") },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GENRES.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(genre) },
                    onClick = {
                        onValueChange(genre)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}