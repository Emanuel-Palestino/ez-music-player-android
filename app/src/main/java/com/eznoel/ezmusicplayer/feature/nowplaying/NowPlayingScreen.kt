package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.R
import com.eznoel.ezmusicplayer.core.common.formatDuration
import com.eznoel.ezmusicplayer.core.designsystem.SongCover
import com.eznoel.ezmusicplayer.core.model.RepeatMode
import com.eznoel.ezmusicplayer.core.model.Song
import com.eznoel.ezmusicplayer.navigation.EditTagsRoute
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NowPlayingScreen(
    onClose: () -> Unit,
    onNavigateToEditTags: (EditTagsRoute) -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState

    // Nada sonando al entrar (o la sesión terminó mientras la pantalla estaba abierta): se cierra sola.
    LaunchedEffect(state) {
        if (state == null) onClose()
    }
    if (state == null) return

    var offsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(Unit) {
                val dismissThresholdPx = size.height * 0.25f
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Solo se permite arrastrar hacia abajo, nunca hacia arriba de su posición natural.
                        offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                    },
                    onDragEnd = {
                        val shouldDismiss = offsetY > dismissThresholdPx
                        scope.launch {
                            if (shouldDismiss) {
                                animate(offsetY, size.height.toFloat(), animationSpec = tween(200)) { value, _ ->
                                    offsetY = value
                                }
                                onClose()
                            } else {
                                animate(offsetY, 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) { value, _ ->
                                    offsetY = value
                                }
                            }
                        }
                    },
                )
            },
    ) {

        NowPlayingContent(
            state = state,
            onClose = onClose,
            onEditTagsClick = {
                onNavigateToEditTags(
                    EditTagsRoute(
                        uriString = state.song.contentUri,
                        fileName = state.song.displayName,
                        relativePath = state.song.relativePath,
                    )
                )
            },
            onPlayPauseClick = viewModel::onPlayPauseClick,
            onNextClick = viewModel::onNextClick,
            onPreviousClick = viewModel::onPreviousClick,
            onShuffleClick = viewModel::onShuffleClick,
            onRepeatClick = viewModel::onRepeatClick,
            onSeek = viewModel::onSeek,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NowPlayingContent(
    state: NowPlayingUiState,
    onClose: () -> Unit,
    onEditTagsClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = state.backgroundColor ?: MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(400),
        label = "now_playing_background",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp),
    ) {
        TopBar(onClose = onClose, onEditTagsClick = onEditTagsClick)
        Spacer(Modifier.height(16.dp))

        Box(contentAlignment = Alignment.BottomStart) {
            SongCover(
                song = state.song,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)),
            )
            FavoriteButton(
                songId = state.song.id,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            state.song.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            state.song.artist.ifBlank { stringResource(R.string.unknown_artist) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(16.dp))
        SeekBar(
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            onSeek = onSeek,
        )

        Spacer(Modifier.height(8.dp))
        PlaybackControls(
            state = state,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onShuffleClick = onShuffleClick,
            onRepeatClick = onRepeatClick,
        )

        Spacer(Modifier.weight(1f))
        BottomInfoRow(song = state.song, queueSize = state.queueSize)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TopBar(onClose: () -> Unit, onEditTagsClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        FilledTonalIconButton(onClick = onClose) {
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Cerrar")
        }
        Box {
            FilledTonalIconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.song_more_options))
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.song_edit_tags)) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    onClick = { menuExpanded = false; onEditTagsClick() },
                )
            }
        }
    }
}

@Composable
private fun FavoriteButton(songId: Long, modifier: Modifier = Modifier) {
    // Solo visual, no persiste: se reinicia si sales de la pantalla o cambia la canción.
    var isFavorite by rememberSaveable(songId) { mutableStateOf(false) }
    FilledTonalIconButton(onClick = { isFavorite = !isFavorite }, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = null,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    // Mientras se arrastra, la posición mostrada es local; al soltar, recién se le pide al player.
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }
    val displayedPositionMs = dragPositionMs ?: positionMs
    val progress = if (durationMs > 0) (displayedPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            stroke = Stroke(width = 8f, cap = StrokeCap.Round),
            trackStroke = Stroke(width = 8f, cap = StrokeCap.Round),
        )
        // Slider transparente: solo maneja el gesto, el dibujo lo hace el indicador de arriba.
        Slider(
            value = progress,
            onValueChange = { dragPositionMs = (it * durationMs).toLong() },
            onValueChangeFinished = {
                dragPositionMs?.let(onSeek)
                dragPositionMs = null
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                disabledThumbColor = Color.Transparent,
                disabledActiveTrackColor = Color.Transparent,
                disabledInactiveTrackColor = Color.Transparent,
            ),
            modifier = Modifier.matchParentSize(),
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatDuration(displayedPositionMs), style = MaterialTheme.typography.labelMedium)
        Text("-${formatDuration((durationMs - displayedPositionMs).coerceAtLeast(0))}", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PlaybackControls(
    state: NowPlayingUiState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onShuffleClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (state.isShuffleEnabled) MaterialTheme.colorScheme.primaryContainer
                else IconButtonDefaults.filledTonalIconButtonColors().containerColor,
            ),
        ) {
            Icon(Icons.Rounded.Shuffle, contentDescription = "Aleatorio")
        }
        FilledTonalIconButton(onClick = onPreviousClick, enabled = state.hasPrevious) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Anterior")
        }
        FilledIconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
        FilledTonalIconButton(onClick = onNextClick, enabled = state.hasNext) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente")
        }
        FilledTonalIconButton(
            onClick = onRepeatClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (state.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primaryContainer
                else IconButtonDefaults.filledTonalIconButtonColors().containerColor,
            ),
        ) {
            Icon(
                imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                contentDescription = "Repetir",
            )
        }
    }
}

@Composable
private fun BottomInfoRow(song: Song, queueSize: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        AssistChip(onClick = {}, enabled = false, label = { Text(song.format) })
        AssistChip(onClick = {}, enabled = false, label = { Text("Up next · $queueSize") })
    }
}