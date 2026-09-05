package com.uol.userapp.features.users.presentation.list

import com.uol.userapp.features.users.domain.model.User

sealed class UsersUiState {
    data object Loading : UsersUiState()
    data class Success(val users: List<User>) : UsersUiState()
    data object Empty : UsersUiState()
    data class Error(val message: String) : UsersUiState()
}