package com.eznoel.ezmusicplayer

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.eznoel.ezmusicplayer.core.image.CoverArtFetcher
import com.eznoel.ezmusicplayer.core.image.CoverArtKeyer
import com.eznoel.ezmusicplayer.data.library.LibraryAutoSync
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EzMusicPlayerApp: Application(), SingletonImageLoader.Factory {

    @Inject lateinit var libraryAutoSync: LibraryAutoSync

    override fun onCreate() {
        super.onCreate() // Hilt inyecta los campos aquí
        libraryAutoSync.start()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(CoverArtKeyer())
                add(CoverArtFetcher.Factory())
            }
            .build()
}