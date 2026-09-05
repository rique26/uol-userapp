package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.model.PhotoResponse
import com.uol.userapp.features.albums.domain.model.Photo

fun PhotoResponse.toDomain(): Photo = Photo(
    id = id,
    albumId = albumId,
    title = title.orEmpty(),
    url = url.orEmpty(),
    thumbnailUrl = thumbnailUrl.orEmpty()
)

fun List<PhotoResponse>.toDomain(): List<Photo> = map { it.toDomain() }