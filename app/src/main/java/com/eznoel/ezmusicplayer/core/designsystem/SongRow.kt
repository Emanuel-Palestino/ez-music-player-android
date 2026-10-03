package com.eznoel.ezmusicplayer.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eznoel.ezmusicplayer.R
import com.eznoel.ezmusicplayer.core.model.Song

@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onEditTagsClick: () -> Unit,
) {
    ListItem(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceContainerLowest),
        leadingContent = {
            SongCover(song = song, modifier = Modifier.size(48.dp))
        },
        shapes = ListItemDefaults.shapes(shape = RoundedCornerShape(16.dp)),
        supportingContent = {
            Text(
                text = song.artist.ifBlank { stringResource(R.string.unknown_artist) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            SongMenu(onEditTagsClick = onEditTagsClick)
        },
    ) {
        Text(
            text = song.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


@Composable
private fun SongMenu(onEditTagsClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.minimumInteractiveComponentSize()
                .size(
                    IconButtonDefaults.smallContainerSize(
                        widthOption = IconButtonDefaults.IconButtonWidthOption.Narrow,
                    )
                ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.song_more_options),
                modifier = Modifier.size(IconButtonDefaults.smallIconSize),
            )
        }
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
                containerColor = MenuDefaults.groupStandardContainerColor,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp), // Defaults 0.dp, 2.dp doesn't look good
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.song_edit_tags)) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    shape = MenuDefaults.itemShape(index = 0, count = 2).shape,
                    onClick = {
                        expanded = false
                        onEditTagsClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.song_add_to_playlist)) },
                    leadingIcon = { Icon(Icons.Rounded.AddCircle, contentDescription = null) },
                    shape = MenuDefaults.itemShape(index = 1, count = 2).shape,
                    onClick = {
                        expanded = false
                    },
                )
            }
        }
    }
}