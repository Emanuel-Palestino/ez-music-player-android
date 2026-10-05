package com.eznoel.ezmusicplayer.core.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eznoel.ezmusicplayer.R

@Composable
fun SongOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditTagsClick: () -> Unit,
    containerColor: Color = MenuDefaults.groupStandardContainerColor,
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 0, count = 1),
            containerColor = containerColor,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp), // Defaults 0.dp, 2.dp doesn't look good
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.song_edit_tags)) },
                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                shape = MenuDefaults.itemShape(index = 0, count = 2).shape,
                onClick = { onEditTagsClick() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.song_add_to_playlist)) },
                leadingIcon = { Icon(Icons.Rounded.AddCircle, contentDescription = null) },
                shape = MenuDefaults.itemShape(index = 1, count = 2).shape,
                onClick = {},
            )
        }
    }
}