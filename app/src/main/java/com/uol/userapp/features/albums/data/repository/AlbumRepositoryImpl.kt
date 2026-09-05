package com.uol.userapp.features.albums.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.ApiException
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.data.local.AlbumDao
import com.uol.userapp.features.albums.data.local.PhotoDao
import com.uol.userapp.features.albums.data.mapper.toDomain
import com.uol.userapp.features.albums.data.mapper.toEntity
import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.albums.domain.model.Photo
import com.uol.userapp.features.albums.domain.repository.AlbumRepository
import java.io.IOException
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val albumDao: AlbumDao,
    private val photoDao: PhotoDao
) : AlbumRepository {

    override suspend fun getAlbumsByUser(userId: Int): Result<List<Album>> = try {
        val response = apiService.getAlbumsByUser(userId)
        if (response.isSuccessful) {
            val albums = response.body().orEmpty()
            albumDao.insertAll(albums.toEntity())
            Result.Success(albums.toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: IOException) {
        val cachedAlbums = albumDao.getByUserId(userId)
        if (cachedAlbums.isNotEmpty()) {
            Result.Success(cachedAlbums.toDomain())
        } else {
            Result.Error(mapThrowable(e))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getPhotosByAlbum(albumId: Int): Result<List<Photo>> = try {
        val response = apiService.getPhotosByAlbum(albumId)
        if (response.isSuccessful) {
            val photos = response.body().orEmpty()
            photoDao.insertAll(photos.toEntity())
            Result.Success(photos.toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: IOException) {
        val cachedPhotos = photoDao.getByAlbumId(albumId)
        if (cachedPhotos.isNotEmpty()) {
            Result.Success(cachedPhotos.toDomain())
        } else {
            Result.Error(mapThrowable(e))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    private fun mapThrowable(throwable: Throwable): Throwable = when (throwable) {
        is IOException -> ApiException(code = -1, message = "Falha de conexão. Verifique sua internet.")
        else -> throwable
    }
}