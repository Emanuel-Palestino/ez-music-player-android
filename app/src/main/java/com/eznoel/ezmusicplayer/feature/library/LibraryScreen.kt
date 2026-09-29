package com.eznoel.ezmusicplayer.feature.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.core.common.formatSize
import com.eznoel.ezmusicplayer.core.designsystem.SongRow
import com.eznoel.ezmusicplayer.core.designsystem.spacing
import com.eznoel.ezmusicplayer.core.permissions.MediaPermissionGate
import com.eznoel.ezmusicplayer.navigation.EditTagsRoute

@Composable
fun LibraryScreen(
    onNavigateToEditTags: (EditTagsRoute) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    MediaPermissionGate {
        LaunchedEffect(Unit) { viewModel.onScreenReady() }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = MaterialTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val summary = (uiState as? LibraryUiState.Content)?.summary
                Text(
                    text = "Biblioteca",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = summary?.let { formatSize(it.totalSizeBytes) }.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.lg))

            ConnectedButtons()
            Spacer(Modifier.height(MaterialTheme.spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.sm))
                    Text("Recientes", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = viewModel::onShuffleClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.sm))
                    Text("Aleatorio")
                }
            }

            Spacer(Modifier.height(MaterialTheme.spacing.md))

            when (uiState) {
                is LibraryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is LibraryUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay archivos en las carpetas incluidas.")
                    }
                }

                is LibraryUiState.Content -> {
                    val songs = (uiState as LibraryUiState.Content).songs
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(
                            items = songs,
                            key = { it.id },
                            contentType = { "song_row" },
                        ) { song ->
                            SongRow(
                                song = song,
                                onClick = { viewModel.onSongClick(song) },
                                onEditTagsClick = {
                                    onNavigateToEditTags(
                                        EditTagsRoute(
                                            uriString = song.contentUri,
                                            fileName = song.displayName,
                                            relativePath = song.relativePath,
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ConnectedButtons() {
    val filters: List<String> = listOf("Canciones", "Álbumes", "Artistas", "Géneros")
    var selected by remember {mutableStateOf("Canciones")}

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        filters.forEachIndexed {index, filter ->
            val isSelected = filter == selected
            ToggleButton(
                checked = isSelected,
                // Selección excluyente: ignoramos el intento de "desmarcar"
                // el chip ya activo, siempre debe quedar uno seleccionado.
                onCheckedChange = { checked -> if (checked) selected = filter },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            ) {
                Text(filter, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}