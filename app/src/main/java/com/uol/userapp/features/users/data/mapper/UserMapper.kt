package com.uol.userapp.features.users.data.mapper

import com.uol.userapp.features.users.data.local.UserEntity
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
@JvmName("userResponseListToDomain")
fun List<UserResponse>.toDomain(): List<User> = map { it.toDomain() }

fun UserResponse.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name.orEmpty(),
    username = username.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    website = website.orEmpty(),
    street = address?.street,
    suite = address?.suite,
    city = address?.city,
    zipcode = address?.zipcode,
    companyName = company?.name,
    companyCatchPhrase = company?.catchPhrase,
    companyBs = company?.bs
)

fun List<UserResponse>.toEntity(): List<UserEntity> = map { it.toEntity() }

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    username = username,
    email = email,
    phone = phone,
    website = website,
    address = street?.let {
        Address(street = it, suite = suite.orEmpty(), city = city.orEmpty(), zipcode = zipcode.orEmpty())
    },
    company = companyName?.let {
        Company(name = it, catchPhrase = companyCatchPhrase.orEmpty(), bs = companyBs.orEmpty())
    }
)
@JvmName("userEntityListToDomain")
fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }