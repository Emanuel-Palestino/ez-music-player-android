package com.eznoel.ezmusicplayer.di

import com.eznoel.ezmusicplayer.playback.PlayerController
import com.eznoel.ezmusicplayer.playback.PlayerControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {
    @Binds
    @Singleton
    abstract fun bindPlayerController(impl: PlayerControllerImpl): PlayerController
}