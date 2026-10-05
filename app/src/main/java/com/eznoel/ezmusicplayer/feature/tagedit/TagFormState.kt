package com.eznoel.ezmusicplayer.feature.tagedit

import com.tagkit.model.AudioTag
import com.tagkit.model.Picture

data class TagFormState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val trackNumber: String = "",
    val year: String = "",
    val genre: String = "",
    val coverBytes: ByteArray? = null,
    val coverMimeType: String? = null,
    val coverChanged: Boolean = false,
)

internal fun AudioTag.toFormState() = TagFormState(
    title = title.orEmpty(),
    artist = artist.orEmpty(),
    album = album.orEmpty(),
    albumArtist = albumArtist.orEmpty(),
    trackNumber = trackNumber.orEmpty(),
    year = year.orEmpty(),
    genre = genre.orEmpty(),
    coverBytes = picture?.data,
    coverMimeType = picture?.mimeType
)

internal fun TagFormState.toAudioTag() = AudioTag(
    title = title, artist = artist, album = album, albumArtist = albumArtist,
    trackNumber = trackNumber, year = year, genre = genre,
    picture = coverBytes?.let { Picture(it, coverMimeType ?: "image/jpeg") }
)