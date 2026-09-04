package com.uol.userapp.features.users.domain.repository

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.domain.model.User

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(userId: Int): Result<User>
}