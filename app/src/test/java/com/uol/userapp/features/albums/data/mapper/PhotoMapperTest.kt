package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.local.PhotoEntity
import com.uol.userapp.features.albums.data.model.PhotoResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoMapperTest {

    /**
     * Objetivo: Garantir que a conversão do DTO de rede (PhotoResponse) para o modelo
     * de domínio (Photo) mapeie corretamente todas as propriedades da foto.
     */
    @Test
    fun `toDomain should map PhotoResponse to Photo domain model correctly`() {
        // Arrange
        val response = PhotoResponse(
            id = 100,
            albumId = 1,
            title = "Foto de Praia",
            url = "https://via.placeholder.com/600",
            thumbnailUrl = "https://via.placeholder.com/150"
        )

        // Act
        val photo = response.toDomain()

        // Assert
        assertEquals(100, photo.id)
        assertEquals(1, photo.albumId)
        assertEquals("Foto de Praia", photo.title)
        assertEquals("https://via.placeholder.com/600", photo.url)
        assertEquals("https://via.placeholder.com/150", photo.thumbnailUrl)
    }

    /**
     * Objetivo: Validar se a conversão do DTO de rede (PhotoResponse) para a entidade
     * de persistência (PhotoEntity) transfere os dados com precisão para gravação no Room.
     */
    @Test
    fun `toEntity should map PhotoResponse to PhotoEntity correctly`() {
        // Arrange
        val response = PhotoResponse(
            id = 200,
            albumId = 2,
            title = "Natureza",
            url = "https://site.com/photo.jpg",
            thumbnailUrl = "https://site.com/thumb.jpg"
        )

        // Act
        val entity = response.toEntity()

        // Assert
        assertEquals(200, entity.id)
        assertEquals(2, entity.albumId)
        assertEquals("Natureza", entity.title)
        assertEquals("https://site.com/photo.jpg", entity.url)
        assertEquals("https://site.com/thumb.jpg", entity.thumbnailUrl)
    }

    /**
     * Objetivo: Verificar se a entidade do banco local (PhotoEntity) é convertida
     * corretamente para o modelo de domínio (Photo) na leitura do cache offline.
     */
    @Test
    fun `toDomain should map PhotoEntity to Photo domain model correctly`() {
        // Arrange
        val entity = PhotoEntity(
            id = 300,
            albumId = 3,
            title = "Montanha",
            url = "https://site.com/mountain.jpg",
            thumbnailUrl = "https://site.com/mountain_thumb.jpg"
        )

        // Act
        val photo = entity.toDomain()

        // Assert
        assertEquals(300, photo.id)
        assertEquals(3, photo.albumId)
        assertEquals("Montanha", photo.title)
        assertEquals("https://site.com/mountain.jpg", photo.url)
        assertEquals("https://site.com/mountain_thumb.jpg", photo.thumbnailUrl)
    }

    /**
     * Objetivo: Confirmar o mapeamento em lote de coleções de fotos para o domínio e para o banco.
     */
    @Test
    fun `list extension functions should map photo lists correctly`() {
        // Arrange
        val responseList = listOf(PhotoResponse(1, 1, "T1", "U1", "TH1"))
        val entityList = listOf(PhotoEntity(2, 1, "T2", "U2", "TH2"))

        // Act
        val domainFromResponses = responseList.toDomain()
        val entitiesFromResponses = responseList.toEntity()
        val domainFromEntities = entityList.toDomain()

        // Assert
        assertEquals(1, domainFromResponses.size)
        assertEquals(1, entitiesFromResponses.size)
        assertEquals(1, domainFromEntities.size)
        assertEquals(1, domainFromResponses.first().id)
        assertEquals(2, domainFromEntities.first().id)
    }
}