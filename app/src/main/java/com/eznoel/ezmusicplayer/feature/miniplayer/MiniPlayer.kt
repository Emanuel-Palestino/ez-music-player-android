package com.eznoel.ezmusicplayer.feature.miniplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eznoel.ezmusicplayer.R
import com.eznoel.ezmusicplayer.core.designsystem.SongCover

@Composable
fun MiniPlayer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState ?: return // nada sonando: no ocupa espacio

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingValues(start = 8.dp, end = 8.dp, bottom = 10.dp)),
        shape = MaterialTheme.shapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongCover(
                    song = state.song,
                    modifier = Modifier
                        .size(IconButtonDefaults.mediumContainerSize())
                        .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.song.artist.ifBlank { stringResource(R.string.unknown_artist) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(
                    onClick = viewModel::onPreviousClick,
                    enabled = state.hasPrevious,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = stringResource(R.string.player_previous),
                        modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                    )
                }

                FilledIconToggleButton(
                    checked = state.isPlaying,
                    onCheckedChange = { viewModel.onPlayPauseClick() },
                    modifier = Modifier.size(
                        IconButtonDefaults.mediumContainerSize(),
                    ),
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            if (state.isPlaying) R.string.player_pause else R.string.player_play
                        ),
                        modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                    )
                }

                IconButton(
                    onClick = viewModel::onNextClick,
                    enabled = state.hasNext,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.player_next),
                        modifier = Modifier.size(IconButtonDefaults.largeIconSize),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .align(Alignment.BottomCenter),
                trackColor = Color.Transparent,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}