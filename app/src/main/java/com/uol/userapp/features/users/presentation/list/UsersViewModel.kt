package com.uol.userapp.features.users.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private var allUsers: List<User> = emptyList()

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading

            when (val result = getUsersUseCase()) {
                is Result.Success -> {
                    allUsers = result.data
                    applyFilter(_searchQuery.value)
                }
                is Result.Error -> {
                    _uiState.value = UsersUiState.Error(
                        result.message ?: "Não foi possível carregar os usuários."
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                        user.username.contains(query, ignoreCase = true) ||
                        user.email.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = if (filtered.isEmpty()) {
            UsersUiState.Empty
        } else {
            UsersUiState.Success(filtered)
        }
    }
}