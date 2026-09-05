package com.uol.userapp.features.albums.domain.repository

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.albums.domain.model.Photo

interface AlbumRepository {
    suspend fun getAlbumsByUser(userId: Int): Result<List<Album>>
    suspend fun getPhotosByAlbum(albumId: Int): Result<List<Photo>>
}