package com.eznoel.ezmusicplayer.data.library

import android.content.ContentUris
import android.provider.MediaStore
import com.eznoel.ezmusicplayer.core.database.SongEntity
import com.eznoel.ezmusicplayer.core.model.Song

internal fun ScannedSong.toEntity(existingId: Long) = SongEntity(
    id = existingId,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    durationMs = durationMs,
    trackNumber = trackNumber,
    year = year,
    displayName = displayName,
    relativePath = relativePath,
    sizeBytes = sizeBytes,
    dateAddedSec = dateAddedSec,
    dateModifiedSec = dateModifiedSec,
    isAvailable = true,
)

internal fun SongEntity.toSong() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    durationMs = durationMs,
    trackNumber = trackNumber,
    year = year,
    format = displayName.substringAfterLast('.', "").uppercase(),
    contentUri = ContentUris
        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaStoreId)
        .toString(),
    displayName = displayName,
    relativePath = relativePath,
    sizeBytes = sizeBytes,
    dateAddedSec = dateAddedSec,
)