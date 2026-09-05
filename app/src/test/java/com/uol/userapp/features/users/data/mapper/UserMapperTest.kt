package com.uol.userapp.features.users.data.mapper

import com.uol.userapp.features.users.data.local.UserEntity
import com.uol.userapp.features.users.data.model.AddressResponse
import com.uol.userapp.features.users.data.model.CompanyResponse
import com.uol.userapp.features.users.data.model.GeoResponse
import com.uol.userapp.features.users.data.model.UserResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserMapperTest {

    private val sampleUserResponse = UserResponse(
        id = 1,
        name = "Pedro Oliveira",
        username = "pedro",
        email = "pedro@email.com",
        phone = "123456789",
        website = "pedro.dev",
        address = AddressResponse(
            street = "Rua A",
            suite = "Apt 101",
            city = "Fortaleza",
            zipcode = "60000-000",
            geo = GeoResponse(lat = "-3.71", lng = "-38.54")
        ),
        company = CompanyResponse(
            name = "UOL",
            catchPhrase = "Tecnologia",
            bs = "Software"
        )
    )

    private val sampleUserEntity = UserEntity(
        id = 1,
        name = "Pedro Oliveira",
        username = "pedro",
        email = "pedro@email.com",
        phone = "123456789",
        website = "pedro.dev",
        street = "Rua A",
        suite = "Apt 101",
        city = "Fortaleza",
        zipcode = "60000-000",
        companyName = "UOL",
        companyCatchPhrase = "Tecnologia",
        companyBs = "Software"
    )

    /**
     * Objetivo: Garantir que o mapeamento completo de UserResponse para o modelo de domínio User
     * converta corretamente todos os campos primários e objetos aninhados (Address e Company).
     */
    @Test
    fun `toDomain should map UserResponse to User domain model correctly when all fields are present`() {
        // Arrange
        val response = sampleUserResponse

        // Act
        val domain = response.toDomain()

        // Assert
        assertEquals(1, domain.id)
        assertEquals("Pedro Oliveira", domain.name)
        assertEquals("pedro", domain.username)
        assertEquals("pedro@email.com", domain.email)
        assertEquals("123456789", domain.phone)
        assertEquals("pedro.dev", domain.website)

        assertNotNull(domain.address)
        assertEquals("Rua A", domain.address?.street)
        assertEquals("Apt 101", domain.address?.suite)
        assertEquals("Fortaleza", domain.address?.city)
        assertEquals("60000-000", domain.address?.zipcode)

        assertNotNull(domain.company)
        assertEquals("UOL", domain.company?.name)
        assertEquals("Tecnologia", domain.company?.catchPhrase)
        assertEquals("Software", domain.company?.bs)
    }

    /**
     * Objetivo: Validar o comportamento defensivo do orEmpty() e do operador safe call (?.),
     * garantindo que campos nulos de texto virem String vazia e objetos complexos nulos permaneçam nulos.
     */
    @Test
    fun `toDomain should handle null values safely in UserResponse`() {
        // Arrange
        val responseWithNulls = UserResponse(
            id = 2,
            name = null,
            username = null,
            email = null,
            address = null,
            phone = null,
            website = null,
            company = null
        )

        // Act
        val domain = responseWithNulls.toDomain()

        // Assert
        assertEquals(2, domain.id)
        assertEquals("", domain.name)
        assertEquals("", domain.username)
        assertEquals("", domain.email)
        assertEquals("", domain.phone)
        assertEquals("", domain.website)
        assertNull(domain.address)
        assertNull(domain.company)
    }

    /**
     * Objetivo: Confirmar se a conversão do DTO (UserResponse) para a entidade plana do Room (UserEntity)
     * achata corretamente as propriedades de Address e Company nos campos de nível raiz.
     */
    @Test
    fun `toEntity should map UserResponse to UserEntity correctly`() {
        // Arrange
        val response = sampleUserResponse

        // Act
        val entity = response.toEntity()

        // Assert
        assertEquals(1, entity.id)
        assertEquals("Pedro Oliveira", entity.name)
        assertEquals("Rua A", entity.street)
        assertEquals("Apt 101", entity.suite)
        assertEquals("Fortaleza", entity.city)
        assertEquals("60000-000", entity.zipcode)
        assertEquals("UOL", entity.companyName)
        assertEquals("Tecnologia", entity.companyCatchPhrase)
        assertEquals("Software", entity.companyBs)
    }

    /**
     * Objetivo: Verificar se a leitura da entidade do banco local (UserEntity) é convertida para o modelo
     * de domínio reconstruindo adequadamente os objetos aninhados Address e Company.
     */
    @Test
    fun `toDomain should map UserEntity to User domain model correctly`() {
        // Arrange
        val entity = sampleUserEntity

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals(1, domain.id)
        assertEquals("Pedro Oliveira", domain.name)
        assertEquals("Rua A", domain.address?.street)
        assertEquals("Apt 101", domain.address?.suite)
        assertEquals("UOL", domain.company?.name)
        assertEquals("Tecnologia", domain.company?.catchPhrase)
    }

    /**
     * Objetivo: Garantir que se a UserEntity tiver campos como street ou companyName nulos,
     * os objetos de domínio correspondentes (Address e Company) resultem em nulo.
     */
    @Test
    fun `toDomain should map UserEntity with null street and companyName to null domain objects`() {
        // Arrange
        val entityWithoutAddressOrCompany = UserEntity(
            id = 3,
            name = "Maria",
            username = "maria",
            email = "maria@email.com",
            phone = "987654321",
            website = "maria.dev",
            street = null,
            suite = null,
            city = null,
            zipcode = null,
            companyName = null,
            companyCatchPhrase = null,
            companyBs = null
        )

        // Act
        val domain = entityWithoutAddressOrCompany.toDomain()

        // Assert
        assertEquals(3, domain.id)
        assertNull(domain.address)
        assertNull(domain.company)
    }

    /**
     * Objetivo: Confirmar que as funções de extensão em coleções (List) realizam o mapeamento
     * em lote mantendo a ordem e quantidade de itens em todas as variantes de conversão.
     */
    @Test
    fun `list extension functions should map user lists correctly`() {
        // Arrange
        val responseList = listOf(sampleUserResponse)
        val entityList = listOf(sampleUserEntity)

        // Act
        val domainFromResponses = responseList.toDomain()
        val entitiesFromResponses = responseList.toEntity()
        val domainFromEntities = entityList.toDomain()

        // Assert
        assertEquals(1, domainFromResponses.size)
        assertEquals(1, entitiesFromResponses.size)
        assertEquals(1, domainFromEntities.size)

        assertEquals(sampleUserResponse.id, domainFromResponses.first().id)
        assertEquals(sampleUserResponse.id, entitiesFromResponses.first().id)
        assertEquals(sampleUserEntity.id, domainFromEntities.first().id)
    }
}