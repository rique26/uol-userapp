package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.local.PhotoEntity
import com.uol.userapp.features.albums.data.model.PhotoResponse
import com.uol.userapp.features.albums.domain.model.Photo

fun PhotoResponse.toDomain(): Photo = Photo(
    id = id,
    albumId = albumId,
    title = title.orEmpty(),
    url = url.orEmpty(),
    thumbnailUrl = thumbnailUrl.orEmpty()
)
@JvmName("photoResponseListToDomain")
fun List<PhotoResponse>.toDomain(): List<Photo> = map { it.toDomain() }

fun PhotoResponse.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    albumId = albumId,
    title = title.orEmpty(),
    url = url.orEmpty(),
    thumbnailUrl = thumbnailUrl.orEmpty()
)
fun List<PhotoResponse>.toEntity(): List<PhotoEntity> = map { it.toEntity() }

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl
)
@JvmName("photoEntityListToDomain")
fun List<PhotoEntity>.toDomain(): List<Photo> = map { it.toDomain() }