package com.uol.userapp.features.albums.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photos WHERE albumId = :albumId")
    suspend fun getByAlbumId(albumId: Int): List<PhotoEntity>
}