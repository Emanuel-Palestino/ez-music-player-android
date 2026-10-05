package com.eznoel.ezmusicplayer.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Casino
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
import com.eznoel.ezmusicplayer.core.model.LibrarySummary
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
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            LibraryHeader((uiState as? LibraryUiState.Content)?.summary)
            Spacer(Modifier.height(20.dp))

            ListViewTabs()
            Spacer(Modifier.height(6.dp))

            SortAndShuffle(viewModel::onShuffleClick)

            Spacer(Modifier.height(10.dp))

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
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 120.dp)
                    ) {
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
                                            songId = song.id,
                                            uriString = song.contentUri,
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
fun LibraryHeader(
    summary: LibrarySummary? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
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
}

@Composable
fun ListViewTabs() {
    val filters: List<String> = listOf("Canciones", "Álbumes", "Artistas")
    var selected by remember {mutableStateOf("Canciones")}

    Row(
        modifier = Modifier
            //.horizontalScroll(rememberScrollState())
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
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
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
            ) {
                Text(
                    filter,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun SortAndShuffle(
    onShuffleClick: () -> Unit,
) {
    val buttonsSize = ButtonDefaults.MinHeight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = {},
            modifier = Modifier.heightIn(min = buttonsSize),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonsSize, hasStartIcon = true),
            shapes = ButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonsSize)),
            )
            Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(buttonsSize)))
            Text("Recientes", style = ButtonDefaults.textStyleFor(buttonsSize))
        }

        Button(
            onClick = onShuffleClick,
            modifier = Modifier.heightIn(min = buttonsSize),
            contentPadding = ButtonDefaults.contentPaddingFor(buttonsSize, hasStartIcon = true),
            shapes = ButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.Casino,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonsSize)),
            )
            Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(buttonsSize)))
            Text("Aleatorio", style = ButtonDefaults.textStyleFor(buttonsSize))
        }
    }
}