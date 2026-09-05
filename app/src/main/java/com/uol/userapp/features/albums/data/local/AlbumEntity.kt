package com.uol.userapp.features.albums.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String
)