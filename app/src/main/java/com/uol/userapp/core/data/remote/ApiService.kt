package com.uol.userapp.core.data.remote

import com.uol.userapp.features.albums.data.model.AlbumResponse
import com.uol.userapp.features.albums.data.model.PhotoResponse
import com.uol.userapp.features.users.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato único com os endpoints consumidos da API pública
 */

interface ApiService {

    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserResponse>

    @GET("albums")
    suspend fun getAlbumsByUser(
        @Query("userId") userId: Int
    ): Response<List<AlbumResponse>>

    @GET("photos")
    suspend fun getPhotosByAlbum(
        @Query("albumId") albumId: Int
    ): Response<List<PhotoResponse>>
}