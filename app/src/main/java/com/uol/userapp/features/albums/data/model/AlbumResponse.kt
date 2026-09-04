package com.uol.userapp.features.albums.data.model

import com.google.gson.annotations.SerializedName

data class AlbumResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)