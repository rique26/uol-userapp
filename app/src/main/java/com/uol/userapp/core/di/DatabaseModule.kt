package com.uol.userapp.core.di

import android.content.Context
import androidx.room.Room
import com.uol.userapp.core.data.local.AppDatabase
import com.uol.userapp.features.albums.data.local.AlbumDao
import com.uol.userapp.features.albums.data.local.PhotoDao
import com.uol.userapp.features.users.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "userapp_database"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .build()

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao = appDatabase.userDao()

    @Provides
    fun provideAlbumDao(appDatabase: AppDatabase): AlbumDao = appDatabase.albumDao()

    @Provides
    fun providePhotoDao(appDatabase: AppDatabase): PhotoDao = appDatabase.photoDao()
}