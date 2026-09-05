package com.uol.userapp.features.albums.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.ApiException
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.data.mapper.toDomain
import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.albums.domain.model.Photo
import com.uol.userapp.features.albums.domain.repository.AlbumRepository
import java.io.IOException
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AlbumRepository {

    override suspend fun getAlbumsByUser(userId: Int): Result<List<Album>> = try {
        val response = apiService.getAlbumsByUser(userId)
        if (response.isSuccessful) {
            Result.Success(response.body().orEmpty().toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: Exception) {
        Result.Error(mapThrowable(e))
    }

    override suspend fun getPhotosByAlbum(albumId: Int): Result<List<Photo>> = try {
        val response = apiService.getPhotosByAlbum(albumId)
        if (response.isSuccessful) {
            Result.Success(response.body().orEmpty().toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: Exception) {
        Result.Error(mapThrowable(e))
    }

    private fun mapThrowable(throwable: Throwable): Throwable = when (throwable) {
        is IOException -> ApiException(code = -1, message = "Falha de conexão. Verifique sua internet.")
        else -> throwable
    }
}