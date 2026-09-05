package com.uol.userapp.features.users.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val street: String?,
    val suite: String?,
    val city: String?,
    val zipcode: String?,
    val companyName: String?,
    val companyCatchPhrase: String?,
    val companyBs: String?
)