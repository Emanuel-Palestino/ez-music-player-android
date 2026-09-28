package com.eznoel.ezmusicplayer.core.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
