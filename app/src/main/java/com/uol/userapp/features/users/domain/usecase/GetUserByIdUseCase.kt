package com.uol.userapp.features.users.domain.usecase

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Result<User> = userRepository.getUserById(userId)
}