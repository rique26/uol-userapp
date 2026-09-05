package com.uol.userapp.features.albums.data.mapper

import com.uol.userapp.features.albums.data.local.AlbumEntity
import com.uol.userapp.features.albums.data.model.AlbumResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumMapperTest {

    /**
     * Objetivo: Garantir que a conversão de um DTO de rede (AlbumResponse) para o modelo
     * de domínio (Album) mapeie corretamente todas as propriedades sem perda de dados.
     */
    @Test
    fun `toDomain should map AlbumResponse to Album domain model correctly`() {
        // Arrange
        val response = AlbumResponse(id = 1, userId = 10, title = "Meu Álbum")

        // Act
        val result = response.toDomain()

        // Assert
        assertEquals(1, result.id)
        assertEquals(10, result.userId)
        assertEquals("Meu Álbum", result.title)
    }

    /**
     * Objetivo: Validar se a conversão do DTO de rede (AlbumResponse) para a entidade
     * de persistência (AlbumEntity) mapeia os dados corretamente para gravação no Room.
     */
    @Test
    fun `toEntity should map AlbumResponse to AlbumEntity correctly`() {
        // Arrange
        val response = AlbumResponse(id = 1, userId = 10, title = "Álbum de Fotos")

        // Act
        val entity = response.toEntity()

        // Assert
        assertEquals(1, entity.id)
        assertEquals(10, entity.userId)
        assertEquals("Álbum de Fotos", entity.title)
    }

    /**
     * Objetivo: Verificar se a entidade do banco local (AlbumEntity) é convertida de forma
     * precisa para o modelo de domínio (Album), garantindo o fluxo de leitura offline.
     */
    @Test
    fun `toDomain should map AlbumEntity to Album domain model correctly`() {
        // Arrange
        val entity = AlbumEntity(id = 5, userId = 20, title = "Viagens")

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals(5, domain.id)
        assertEquals(20, domain.userId)
        assertEquals("Viagens", domain.title)
    }

    /**
     * Objetivo: Confirmar se as funções de extensão em coleções (List) convertem múltiplos
     * itens preservando a quantidade e a integridade de cada elemento na lista.
     */
    @Test
    fun `list extension functions should map lists correctly`() {
        // Arrange
        val responseList = listOf(AlbumResponse(id = 1, userId = 1, title = "A"))
        val entityList = listOf(AlbumEntity(id = 2, userId = 2, title = "B"))

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