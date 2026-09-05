package com.uol.userapp.features.albums.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.usecase.GetPhotosByAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPhotosByAlbumUseCase: GetPhotosByAlbumUseCase
) : ViewModel() {

    private val albumId: Int = checkNotNull(savedStateHandle["albumId"])

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = AlbumDetailUiState.Loading

            when (val result = getPhotosByAlbumUseCase(albumId)) {
                is Result.Success -> {
                    _uiState.value = if (result.data.isEmpty()) {
                        AlbumDetailUiState.Empty
                    } else {
                        AlbumDetailUiState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.value = AlbumDetailUiState.Error(
                        result.message ?: "Não foi possível carregar as fotos."
                    )
                }
                Result.Loading -> Unit
            }
        }
    }
}