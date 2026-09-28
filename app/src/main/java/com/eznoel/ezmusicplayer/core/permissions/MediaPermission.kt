package com.eznoel.ezmusicplayer.core.permissions

import android.Manifest
import android.os.Build

object MediaPermission {
    val permissionName: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}