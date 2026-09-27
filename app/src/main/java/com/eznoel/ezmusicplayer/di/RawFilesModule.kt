package com.eznoel.ezmusicplayer.di

import com.eznoel.ezmusicplayer.data.rawfiles.RawFilesRepository
import com.eznoel.ezmusicplayer.data.rawfiles.RawFilesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RawFilesModule {
    @Binds
    abstract fun bindRawFilesRepository(impl: RawFilesRepositoryImpl): RawFilesRepository
}