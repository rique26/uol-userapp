package com.uol.userapp.features.users.di

import com.uol.userapp.features.users.domain.repository.UserRepository
import com.uol.userapp.features.users.domain.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}