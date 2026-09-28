package com.eznoel.ezmusicplayer.di

import com.eznoel.ezmusicplayer.data.library.LibraryRepository
import com.eznoel.ezmusicplayer.data.library.LibraryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {
    @Binds
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository
}