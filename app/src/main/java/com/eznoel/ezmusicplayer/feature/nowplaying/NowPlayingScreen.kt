package com.eznoel.ezmusicplayer.feature.nowplaying

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
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
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.R
import com.eznoel.ezmusicplayer.core.common.formatDuration
import com.eznoel.ezmusicplayer.core.designsystem.SongCover
import com.eznoel.ezmusicplayer.core.designsystem.SongOptionsMenu
import com.eznoel.ezmusicplayer.core.image.rememberCoverColorScheme
import com.eznoel.ezmusicplayer.core.model.RepeatMode
import com.eznoel.ezmusicplayer.core.model.Song
import com.eznoel.ezmusicplayer.navigation.EditTagsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
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
                        songId = state.song.id,
                        uriString = state.song.contentUri,
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
    // Color schema derived from the covert art (animated)
    val colorScheme = rememberCoverColorScheme(state.coverSeeds)

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background,
        ) {
            // No systemBars padding at the top: the cover is full bleed behind the status bar
            Column(modifier = Modifier.fillMaxSize()) {
                CoverHeader(
                    song = state.song,
                    backgroundColor = colorScheme.background,
                    onClose = onClose,
                    onEditTagsClick = onEditTagsClick,
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp),
                ) {
                    Text(
                        state.song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        state.song.artist.ifBlank { stringResource(R.string.unknown_artist) },
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(16.dp))
                    SeekBar(
                        positionMs = state.positionMs,
                        durationMs = state.durationMs,
                        isPlaying = state.isPlaying,
                        onSeek = onSeek,
                    )

                    Spacer(Modifier.height(16.dp))
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
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CoverHeader(
    song: Song,
    backgroundColor: Color,
    onClose: () -> Unit,
    onEditTagsClick: () -> Unit,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LightStatusBarIconsEffect()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val coverHeight = maxWidth + topInset

        SongCover(
            song = song,
            modifier = Modifier
                .fillMaxWidth()
                .height(coverHeight),
        )

        // Dark gradient at the top guarantees contrast for the status bar icons and buttons
        val gradientHeight = topInset + 100.dp
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(gradientHeight)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.40f),
                        0.2f to Color.Black.copy(alpha = 0.26f),
                        0.4f to Color.Black.copy(alpha = 0.14f),
                        0.6f to Color.Black.copy(alpha = 0.06f),
                        0.8f to Color.Black.copy(alpha = 0.02f),
                        1.0f to Color.Black.copy(alpha = 0.0f),
                    )
                ),
        )

        // Bottom fade from the cover to the background color
        val fadeHeight = coverHeight * 0.4f
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(fadeHeight)
                .background(
                    Brush.verticalGradient(
                        0f to backgroundColor.copy(alpha = 0f),
                        0.30f to backgroundColor.copy(alpha = 0.20f),
                        0.60f to backgroundColor.copy(alpha = 0.65f),
                        0.85f to backgroundColor.copy(alpha = 0.92f),
                        1f to backgroundColor,
                    )
                ),
        )

        CoverTopBar(
            onClose = onClose,
            onEditTagsClick = onEditTagsClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        FavoriteButton(
            songId = song.id,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp), // alineado con el título
        )
    }
}

/** Forces light status bar icons while this screen is composed, and restores the previous value afterward. */
@Composable
private fun LightStatusBarIconsEffect() {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    DisposableEffect(window) {
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false // false = íconos claros
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}

@Composable
private fun CoverTopBar(
    onClose: () -> Unit,
    onEditTagsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TopBarButton(
            onClick = onClose,
            icon = Icons.Rounded.KeyboardArrowDown,
            description = "Cerrar",
        )

        Box {
            TopBarButton(
                onClick = { menuExpanded = true },
                icon = Icons.Rounded.MoreVert,
                description = stringResource(R.string.song_more_options),
            )
            SongOptionsMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onEditTagsClick = {
                    menuExpanded = false
                    onEditTagsClick()
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

@Composable
private fun TopBarButton(onClick: () -> Unit, icon: ImageVector, description: String) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shapes = IconButtonDefaults.shapes(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(IconButtonDefaults.largeIconSize),
        )
    }
}

@Composable
private fun FavoriteButton(songId: Long, modifier: Modifier = Modifier) {
    var isFavorite by rememberSaveable(songId) { mutableStateOf(false) }

    FilledTonalIconToggleButton(
        checked = isFavorite,
        onCheckedChange = { isFavorite = it },
        modifier = modifier.size(
            IconButtonDefaults.mediumContainerSize(),
        ),
        shapes = IconButtonDefaults.toggleableShapes(),
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            checkedContainerColor = MaterialTheme.colorScheme.primary,
            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
) {
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }
    // Posición del último seek, que se mantiene hasta que el player la alcance.
    var pendingSeekMs by remember { mutableStateOf<Long?>(null) }

    val displayedPositionMs = dragPositionMs ?: pendingSeekMs ?: positionMs
    val progress =
        if (durationMs > 0) (displayedPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    // Se libera cuando el player llega a la posición pedida...
    LaunchedEffect(positionMs) {
        pendingSeekMs?.let { if (abs(positionMs - it) < 1000) pendingSeekMs = null }
    }
    // ...o por timeout, por si el seek falla o el player queda en otro punto.
    LaunchedEffect(pendingSeekMs) {
        if (pendingSeekMs != null) {
            delay(1500)
            pendingSeekMs = null
        }
    }

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 6.dp.toPx() }
    val stroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }

    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "waveAmplitude",
    )

    val isInteracting = dragPositionMs != null
    val thumbWidth by animateDpAsState(
        targetValue = if (isInteracting) 4.dp else 6.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "thumbWidth",
    )

    val currentOnSeek by rememberUpdatedState(onSeek)
    val currentDurationMs by rememberUpdatedState(durationMs)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    val target = (fraction * currentDurationMs).toLong()
                    pendingSeekMs = target
                    currentOnSeek(target)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        dragPositionMs = (fraction * currentDurationMs).toLong()
                    },
                    onHorizontalDrag = { change, _ ->
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragPositionMs = (fraction * currentDurationMs).toLong()
                    },
                    onDragEnd = {
                        dragPositionMs?.let { target ->
                            // Primero se fija el pending y luego se limpia el drag,
                            // así no hay ningún frame mostrando la posición vieja.
                            pendingSeekMs = target
                            currentOnSeek(target)
                        }
                        dragPositionMs = null
                    },
                    onDragCancel = { dragPositionMs = null },
                )
            },
    ) {
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            amplitude = { amplitude },
            stroke = stroke,
            trackStroke = stroke,
            gapSize = 8.dp,
        )
        // Thumb más grueso (6.dp) y desplazado 2.dp a la izquierda para tapar la onda.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = maxWidth * progress - thumbWidth / 2 + 2.dp)
                .size(width = thumbWidth, height = 24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatDuration(displayedPositionMs), style = MaterialTheme.typography.labelMedium)
        Text(
            "-${formatDuration((durationMs - displayedPositionMs).coerceAtLeast(0))}",
            style = MaterialTheme.typography.labelMedium,
        )
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
    val activeTint = MaterialTheme.colorScheme.primary
    val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShuffleClick, shapes = IconButtonDefaults.shapes()) {
            Icon(
                Icons.Rounded.Shuffle,
                contentDescription = "Aleatorio",
                tint = if (state.isShuffleEnabled) activeTint else inactiveTint,
            )
        }
        SkipButton(Icons.Rounded.SkipPrevious, "Anterior", enabled = state.hasPrevious, onClick = onPreviousClick)
        FilledIconToggleButton(
            checked = state.isPlaying,
            onCheckedChange = { onPlayPauseClick() },
            //modifier = Modifier.size(width = 88.dp, height = 72.dp),
            modifier = Modifier.size(
                IconButtonDefaults.largeContainerSize(
                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                ),
            ),
            shapes = IconButtonDefaults.toggleableShapes(
                checkedShape = RoundedCornerShape(32.dp)
            ),
            colors = IconButtonDefaults.filledIconToggleButtonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
        SkipButton(Icons.Rounded.SkipNext, "Siguiente", enabled = state.hasNext, onClick = onNextClick)
        IconButton(onClick = onRepeatClick, shapes = IconButtonDefaults.shapes()) {
            Icon(
                imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                contentDescription = "Repetir",
                tint = if (state.repeatMode != RepeatMode.OFF) activeTint else inactiveTint,
            )
        }
    }
}

@Composable
private fun SkipButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        //modifier = Modifier.size(width = 64.dp, height = 56.dp),
        modifier = Modifier.size(
            IconButtonDefaults.mediumContainerSize()
        ),
        shapes = IconButtonDefaults.shapes(shape = IconButtonDefaults.mediumSquareShape)
    ) {
        Icon(icon, contentDescription = description)
    }
}

@Composable
private fun BottomInfoRow(song: Song, queueSize: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(song.format) },
            leadingIcon = { Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
        AssistChip(
            onClick = {},
            enabled = false,
            label = { Text("Cola · $queueSize") },
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = AssistChipDefaults.assistChipColors(
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            border = null,
        )
    }
}