package com.uol.userapp.core.domain.util

/**
 * Wrapper genérico e imutável para o resultado de uma operação (tipicamente
 * uma chamada de rede executada por um UseCase).
 */

sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(
        val throwable: Throwable,
        val message: String? = throwable.message
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, String?) -> Unit): Result<T> {
    if (this is Result.Error) action(throwable, message)
    return this
}