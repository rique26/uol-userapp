package com.uol.userapp.features.albums.di

import com.uol.userapp.features.albums.data.repository.AlbumRepositoryImpl
import com.uol.userapp.features.albums.domain.repository.AlbumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AlbumModule {

    @Binds
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository
}