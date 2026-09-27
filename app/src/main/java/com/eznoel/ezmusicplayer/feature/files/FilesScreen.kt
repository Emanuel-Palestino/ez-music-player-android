package com.eznoel.ezmusicplayer.feature.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.core.model.RawAudioFile

@Composable
fun FilesScreen(
    onNavigateToFolderConfig: () -> Unit,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    MediaPermissionGate {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Archivos",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToFolderConfig) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Configurar carpetas")
                    }
                }
            )

            when (uiState) {
                is FilesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is FilesUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay archivos en las carpetas incluidas.")
                    }
                }
                is FilesUiState.Content -> {
                    val files = (uiState as FilesUiState.Content).files
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(
                            items = files,
                            key = { it.id },
                            contentType = { "file_row" }
                        ) { file ->
                            RawFileRow(file = file, onEditClick = {  })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RawFileRow(
    file: RawAudioFile,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(file.displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                "${file.relativePath} · ${formatDuration(file.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Rounded.Edit, contentDescription = "Editar tags")
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}