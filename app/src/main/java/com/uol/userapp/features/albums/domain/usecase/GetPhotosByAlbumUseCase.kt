package com.uol.userapp.features.albums.domain.usecase

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.model.Photo
import com.uol.userapp.features.albums.domain.repository.AlbumRepository
import javax.inject.Inject

class GetPhotosByAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Result<List<Photo>> =
        albumRepository.getPhotosByAlbum(albumId)
}