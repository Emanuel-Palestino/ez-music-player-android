package com.eznoel.ezmusicplayer.di

import android.content.Context
import androidx.room.Room
import com.eznoel.ezmusicplayer.core.database.AppDatabase
import com.eznoel.ezmusicplayer.core.database.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ezmusicplayer.db")
            .build() // sin fallbackToDestructiveMigration, por regla del proyecto

    @Provides
    fun provideSongDao(database: AppDatabase): SongDao = database.songDao()
}