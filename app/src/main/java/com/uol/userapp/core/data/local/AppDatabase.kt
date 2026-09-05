package com.uol.userapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uol.userapp.features.albums.data.local.AlbumDao
import com.uol.userapp.features.albums.data.local.AlbumEntity
import com.uol.userapp.features.albums.data.local.PhotoDao
import com.uol.userapp.features.albums.data.local.PhotoEntity
import com.uol.userapp.features.users.data.local.UserDao
import com.uol.userapp.features.users.data.local.UserEntity

@Database(
    entities = [UserEntity::class, AlbumEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao
    abstract fun photoDao(): PhotoDao
}