package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.local.AlbumEntity
import com.uol.userapp.features.albums.data.model.AlbumResponse
import com.uol.userapp.features.albums.domain.model.Album

fun AlbumResponse.toDomain(): Album = Album(
    id = id,
    userId = userId,
    title = title.orEmpty()
)
@JvmName("albumResponseListToDomain")
fun List<AlbumResponse>.toDomain(): List<Album> = map { it.toDomain() }

fun AlbumResponse.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    userId = userId,
    title = title.orEmpty()
)
fun List<AlbumResponse>.toEntity(): List<AlbumEntity> = map { it.toEntity() }

fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    userId = userId,
    title = title
)
@JvmName("albumEntityListToDomain")
fun List<AlbumEntity>.toDomain(): List<Album> = map { it.toDomain() }