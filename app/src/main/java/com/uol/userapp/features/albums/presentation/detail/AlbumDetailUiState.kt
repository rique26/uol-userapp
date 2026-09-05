package com.uol.userapp.features.albums.presentation.detail

import com.uol.userapp.features.albums.domain.model.Photo

sealed class AlbumDetailUiState {
    data object Loading : AlbumDetailUiState()
    data class Success(val photos: List<Photo>) : AlbumDetailUiState()
    data object Empty : AlbumDetailUiState()
    data class Error(val message: String) : AlbumDetailUiState()
}