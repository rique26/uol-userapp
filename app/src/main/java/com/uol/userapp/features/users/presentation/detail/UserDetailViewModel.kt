package com.uol.userapp.features.users.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.usecase.GetAlbumsByUserUseCase
import com.uol.userapp.features.users.domain.usecase.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getAlbumsByUserUseCase: GetAlbumsByUserUseCase
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUserDetail()
    }

    fun loadUserDetail() {
        viewModelScope.launch {
            _uiState.value = UserDetailUiState.Loading

            val userResult = getUserByIdUseCase(userId)
            val albumsResult = getAlbumsByUserUseCase(userId)

            when {
                userResult is Result.Success && albumsResult is Result.Success -> {
                    _uiState.value = UserDetailUiState.Success(
                        user = userResult.data,
                        albums = albumsResult.data
                    )
                }
                userResult is Result.Error -> {
                    _uiState.value = UserDetailUiState.Error(
                        userResult.message ?: "Não foi possível carregar o usuário."
                    )
                }
                albumsResult is Result.Error -> {
                    _uiState.value = UserDetailUiState.Error(
                        albumsResult.message ?: "Não foi possível carregar os álbuns."
                    )
                }
                else -> Unit
            }
        }
    }
}