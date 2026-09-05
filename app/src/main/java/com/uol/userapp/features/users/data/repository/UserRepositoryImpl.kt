package com.uol.userapp.features.users.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.ApiException
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.data.mapper.toDomain
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.repository.UserRepository
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = try {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            Result.Success(response.body().orEmpty().toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: Exception) {
        Result.Error(mapThrowable(e))
    }

    override suspend fun getUserById(userId: Int): Result<User> = try {
        val response = apiService.getUserById(userId)
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.Success(body.toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: Exception) {
        Result.Error(mapThrowable(e))
    }

    private fun mapThrowable(throwable: Throwable): Throwable = when (throwable) {
        is IOException -> ApiException(
            code = -1,
            message = "Falha de conexão. Verifique sua internet."
        )
        else -> throwable
    }
}