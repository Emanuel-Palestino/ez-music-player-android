package com.eznoel.ezmusicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eznoel.ezmusicplayer.core.designsystem.EzMusicPlayerTheme
import com.eznoel.ezmusicplayer.navigation.AppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EzMusicPlayerTheme() {
                AppRoot()
            }
        }
    }
}