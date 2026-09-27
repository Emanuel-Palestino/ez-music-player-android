package com.eznoel.ezmusicplayer.feature.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.core.designsystem.spacing
import com.eznoel.ezmusicplayer.core.model.LibraryFolder

@Composable
fun FolderConfigScreen(
    onBackClick: () -> Unit,
    viewModel: FolderConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FolderConfigContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onFolderToggled = viewModel::onFolderToggled,
        onRescanRequested = viewModel::onRescanRequested,
    )
}

@Composable
fun FolderConfigContent(
    uiState: FolderConfigUiState,
    onBackClick: () -> Unit,
    onFolderToggled: (relativePath: String, included: Boolean) -> Unit,
    onRescanRequested: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Carpetas de biblioteca") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                val isRescanning = (uiState as? FolderConfigUiState.Content)?.isRescanning ?: false
                IconButton(onClick = onRescanRequested, enabled = !isRescanning) {
                    if (isRescanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reescanear")
                    }
                }
            },
        )

        Box(Modifier.weight(1f)) {
            when (uiState) {
                is FolderConfigUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is FolderConfigUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontró música en el dispositivo.\nToca reescanear si acabas de agregar archivos.")
                    }
                }

                is FolderConfigUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = MaterialTheme.spacing.sm,
                        ),
                    ) {
                        items(
                            items = uiState.folders,
                            key = { it.relativePath },
                            contentType = { "folder_row" }
                        ) { folder ->
                            FolderRow(
                                folder = folder,
                                onToggle = { included ->
                                    onFolderToggled(
                                        folder.relativePath,
                                        included
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderRow(
    folder: LibraryFolder,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(folder.relativePath, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${folder.trackCount} canciones · ${formatSize(folder.totalSizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = folder.isIncluded, onCheckedChange = onToggle)
    }
}

private fun formatSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) "%.1f GB".format(mb / 1024.0) else "%.0f MB".format(mb)
}