package com.uol.userapp.features.albums.domain.usecase

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.albums.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumsByUserUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(userId: Int): Result<List<Album>> =
        albumRepository.getAlbumsByUser(userId)
}