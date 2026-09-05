package com.uol.userapp.features.users.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.ApiException
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.data.local.UserDao
import com.uol.userapp.features.users.data.mapper.toDomain
import com.uol.userapp.features.users.data.mapper.toEntity
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.repository.UserRepository
import java.io.IOException
import javax.inject.Inject

/**
 * Single Source of Truth simplificado: sempre tenta a rede primeiro; o Room
 * só entra como fallback quando a falha é de CONECTIVIDADE (IOException).
 * Um erro HTTP normal (ex: 500) segue retornando Error direto — o servidor
 * respondeu, então não é um cenário "offline".
 */
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = try {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            val users = response.body().orEmpty()
            userDao.insertAll(users.toEntity())
            Result.Success(users.toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: IOException) {
        val cachedUsers = userDao.getAll()
        if (cachedUsers.isNotEmpty()) {
            Result.Success(cachedUsers.toDomain())
        } else {
            Result.Error(mapThrowable(e))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getUserById(userId: Int): Result<User> = try {
        val response = apiService.getUserById(userId)
        val body = response.body()
        if (response.isSuccessful && body != null) {
            userDao.insertAll(listOf(body.toEntity()))
            Result.Success(body.toDomain())
        } else {
            Result.Error(ApiException(response.code(), response.message()))
        }
    } catch (e: IOException) {
        val cachedUser = userDao.getById(userId)
        if (cachedUser != null) {
            Result.Success(cachedUser.toDomain())
        } else {
            Result.Error(mapThrowable(e))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    private fun mapThrowable(throwable: Throwable): Throwable = when (throwable) {
        is IOException -> ApiException(
            code = -1,
            message = "Falha de conexão. Verifique sua internet."
        )
        else -> throwable
    }
}