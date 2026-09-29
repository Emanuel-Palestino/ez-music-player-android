package com.eznoel.ezmusicplayer.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class SongStatsRow(val songCount: Int, val totalSizeBytes: Long)

data class FolderRow(
    val relativePath: String,
    val trackCount: Int,
    val totalSizeBytes: Long,
)

@Dao
interface SongDao {

    @Query(
        """
        SELECT * FROM songs
        WHERE isAvailable = 1 AND relativePath NOT IN (:excludedPaths)
        ORDER BY dateAddedSec DESC, id DESC
        """
    )
    fun observeSongs(excludedPaths: List<String>): Flow<List<SongEntity>>

    @Query(
        """
        SELECT COUNT(*) AS songCount, COALESCE(SUM(sizeBytes), 0) AS totalSizeBytes
        FROM songs
        WHERE isAvailable = 1 AND relativePath NOT IN (:excludedPaths)
        """
    )
    fun observeStats(excludedPaths: List<String>): Flow<SongStatsRow>

    @Query("SELECT * FROM songs")
    suspend fun getAll(): List<SongEntity>

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("UPDATE songs SET isAvailable = 0 WHERE id IN (:ids)")
    suspend fun markUnavailable(ids: List<Long>)

    @Query(
        """
    SELECT relativePath, COUNT(*) AS trackCount, SUM(sizeBytes) AS totalSizeBytes
    FROM songs
    WHERE isAvailable = 1
    GROUP BY relativePath
    ORDER BY relativePath
    """
    )
    fun observeFolders(): Flow<List<FolderRow>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: Long): SongEntity?
}