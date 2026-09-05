package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.local.PhotoEntity
import com.uol.userapp.features.albums.data.model.PhotoResponse
import com.uol.userapp.features.albums.domain.model.Photo

/**
 * Percebi durante o desenvolvimento que o host original das fotos retornado pela
 * API (via.placeholder.com) está com falhas conhecidas e persistentes de certificado
 * SSL — problema documentado pela comunidade, não uma instabilidade da minha rede.
 *
 * Como isso impedia demonstrar corretamente a tela de álbum (grid via Picasso e
 * abertura da foto no app padrão do dispositivo), tomei a liberdade de sanitizar a
 * URL aqui na entrada dos dados, trocando por um provedor estável (Picsum Photos).
 * Usar o id da foto como seed mantém a URL determinística — a mesma foto sempre
 * aparece pro mesmo id, preservando o cache do Picasso e do Room.
 *
 * Mais detalhes no README, na seção de Estratégia Offline-First.
 */

private fun sanitizedPhotoUrl(id: Int, size: Int): String =
    "https://picsum.photos/seed/$id/$size"

fun PhotoResponse.toDomain(): Photo = Photo(
    id = id,
    albumId = albumId,
    title = title,
    url = sanitizedPhotoUrl(id, size = 600),
    thumbnailUrl = sanitizedPhotoUrl(id, size = 150)
)
@JvmName("photoResponseListToDomain")
fun List<PhotoResponse>.toDomain(): List<Photo> = map { it.toDomain() }

fun PhotoResponse.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl
)
fun List<PhotoResponse>.toEntity(): List<PhotoEntity> = map { it.toEntity() }

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    albumId = albumId,
    title = title,
    url = sanitizedPhotoUrl(id, size = 600),
    thumbnailUrl = sanitizedPhotoUrl(id, size = 150)
)
@JvmName("photoEntityListToDomain")
fun List<PhotoEntity>.toDomain(): List<Photo> = map { it.toDomain() }