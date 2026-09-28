package com.eznoel.ezmusicplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}