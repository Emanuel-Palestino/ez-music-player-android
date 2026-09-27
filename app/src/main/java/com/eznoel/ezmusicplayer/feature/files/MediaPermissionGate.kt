package com.eznoel.ezmusicplayer.feature.files

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eznoel.ezmusicplayer.core.common.MediaPermission

@Composable
fun rememberMediaPermissionState(): MediaPermissionState {
    val context = LocalContext.current
    var status by remember {
        mutableStateOf(
        if (ContextCompat.checkSelfPermission(context, MediaPermission.permissionName) === PackageManager.PERMISSION_GRANTED) PermissionStatus.Granted else PermissionStatus.NotRequested
        )
    }

    var launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        granted ->
        status = if (granted) {
            PermissionStatus.Granted
        } else {
            val activity = context as? Activity
            val canAskAgain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, MediaPermission.permissionName)
            } ?: true
            if (canAskAgain) PermissionStatus.Denied else PermissionStatus.DeniedPermanently
        }
    }

    return remember(status) {
        MediaPermissionState(
            status = status,
            requestPermission = {launcher.launch(MediaPermission.permissionName)}
        )
    }
}

enum class PermissionStatus { NotRequested, Granted, Denied, DeniedPermanently }

data class MediaPermissionState(
    val status: PermissionStatus,
    val requestPermission: () -> Unit
)

@Composable
fun MediaPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val permissionState = rememberMediaPermissionState()

    // Pide el permiso automáticamente la primera vez que se entra a esta sección.
    LaunchedEffect(Unit) {
        if (permissionState.status == PermissionStatus.NotRequested) {
            permissionState.requestPermission()
        }
    }

    when (permissionState.status) {
        PermissionStatus.Granted -> content()
        PermissionStatus.NotRequested, PermissionStatus.Denied -> {
            PermissionRationale(modifier = modifier, onRequestClick = permissionState.requestPermission)
        }
        PermissionStatus.DeniedPermanently -> {
            PermissionPermanentlyDenied(modifier = modifier)
        }
    }
}

@Composable
private fun PermissionRationale(
    modifier: Modifier = Modifier,
    onRequestClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Acceso a tu música", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "EzMusicPlayer necesita permiso para leer los archivos de audio de tu dispositivo.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestClick) { Text("Conceder permiso") }
    }
}

@Composable
private fun PermissionPermanentlyDenied(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permiso bloqueado", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Actívalo manualmente en Ajustes del sistema para poder escanear tu música.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }) { Text("Abrir ajustes") }
    }
}