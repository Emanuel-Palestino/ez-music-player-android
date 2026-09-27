package com.eznoel.ezmusicplayer.feature.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FilesScreen(onNavigateToFolderConfig: () -> Unit) {
    MediaPermissionGate {
        Column() {
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

            Box(Modifier.fillMaxSize())
        }
    }
}
