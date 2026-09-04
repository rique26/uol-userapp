package com.uol.userapp.core.domain.util

class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)