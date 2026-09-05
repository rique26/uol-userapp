package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.model.AlbumResponse
import com.uol.userapp.features.albums.domain.model.Album

fun AlbumResponse.toDomain(): Album = Album(
    id = id,
    userId = userId,
    title = title.orEmpty()
)

fun List<AlbumResponse>.toDomain(): List<Album> = map { it.toDomain() }