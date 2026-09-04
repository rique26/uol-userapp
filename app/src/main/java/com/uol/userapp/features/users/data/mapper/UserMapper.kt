package com.uol.userapp.features.users.data.mapper

import com.uol.userapp.features.users.data.model.UserResponse
import com.uol.userapp.features.users.domain.model.Address
import com.uol.userapp.features.users.domain.model.Company
import com.uol.userapp.features.users.domain.model.User

fun UserResponse.toDomain(): User = User(
    id = id,
    name = name.orEmpty(),
    username = username.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    website = website.orEmpty(),
    address = address?.let {
        Address(
            street = it.street.orEmpty(),
            suite = it.suite.orEmpty(),
            city = it.city.orEmpty(),
            zipcode = it.zipcode.orEmpty()
        )
    },
    company = company?.let {
        Company(
            name = it.name.orEmpty(),
            catchPhrase = it.catchPhrase.orEmpty(),
            bs = it.bs.orEmpty()
        )
    }
)

fun List<UserResponse>.toDomain(): List<User> = map { it.toDomain() }