package com.uol.userapp.features.albums.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albums WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): List<AlbumEntity>
}