package com.uol.userapp.features.users.presentation.detail

import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.users.domain.model.User

sealed class UserDetailUiState {
    data object Loading : UserDetailUiState()
    data class Success(val user: User, val albums: List<Album>) : UserDetailUiState()
    data class Error(val message: String) : UserDetailUiState()
}