package com.uol.userapp.features.users.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.data.local.UserDao
import com.uol.userapp.features.users.data.local.UserEntity
import com.uol.userapp.features.users.data.mapper.toEntity
import com.uol.userapp.features.users.data.model.UserResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class UserRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        userDao = mockk(relaxed = true)
        repository = UserRepositoryImpl(apiService, userDao)
    }

    private fun <T> httpError(code: Int = 500): Response<T> =
        Response.error(code, "erro".toResponseBody("application/json".toMediaTypeOrNull()))

    private fun fakeUserResponse(id: Int = 1) = UserResponse(
        id = id, name = "Nome $id", username = "user$id", email = "e$id@mail.com",
        phone = "123", website = "site.com", address = null, company = null
    )

    private fun fakeUserEntity(id: Int = 1, name: String = "Cache") = UserEntity(
        id = id, name = name, username = "u", email = "e@mail.com", phone = "123", website = "site",
        street = null, suite = null, city = null, zipcode = null,
        companyName = null, companyCatchPhrase = null, companyBs = null
    )

    // ========================================================================
    // TESTES PARA getUsers()
    // ========================================================================

    /**
     * Objetivo: Confirmar que em uma chamada de rede bem-sucedida, o repositório
     * mapeia e retorna a lista de usuários, salvando o resultado no banco Room.
     */
    @Test
    fun `getUsers should return mapped list and save to cache when API call succeeds`() = runTest {
        // Arrange
        val responses = listOf(fakeUserResponse(1), fakeUserResponse(2))
        coEvery { apiService.getUsers() } returns Response.success(responses)

        // Act
        val result = repository.getUsers()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        coVerify(exactly = 1) { userDao.insertAll(responses.toEntity()) }
    }

    /**
     * Objetivo: Validar que quando a API responde com erro HTTP (ex: 500),
     * o repositório retorna Result.Error sem consultar nem salvar no cache local.
     */
    @Test
    fun `getUsers should return Error and not access cache when API returns HTTP error`() = runTest {
        // Arrange
        coEvery { apiService.getUsers() } returns httpError(500)

        // Act
        val result = repository.getUsers()

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { userDao.getAll() }
        coVerify(exactly = 0) { userDao.insertAll(any()) }
    }

    /**
     * Objetivo: Garantir o fallback offline. Quando ocorre uma falha de conexão (IOException),
     * o repositório recupera os usuários previamente salvos no banco local.
     */
    @Test
    fun `getUsers should fallback to cache when IOException occurs and cache is populated`() = runTest {
        // Arrange
        coEvery { apiService.getUsers() } throws IOException("sem conexão")
        coEvery { userDao.getAll() } returns listOf(fakeUserEntity(1, "Nome em cache"))

        // Act
        val result = repository.getUsers()

        // Assert
        assertTrue(result is Result.Success)
        assertEquals("Nome em cache", (result as Result.Success).data.first().name)
    }

    /**
     * Objetivo: Verificar que se a requisição falhar por falta de conexão e o cache estiver vazio,
     * o repositório retorna Result.Error com mensagem amigável para a UI.
     */
    @Test
    fun `getUsers should return Error when IOException occurs and cache is empty`() = runTest {
        // Arrange
        coEvery { apiService.getUsers() } throws IOException("sem conexão")
        coEvery { userDao.getAll() } returns emptyList()

        // Act
        val result = repository.getUsers()

        // Assert
        assertTrue(result is Result.Error)
        assertEquals("Falha de conexão. Verifique sua internet.", (result as Result.Error).message)
    }

    /**
     * Objetivo: Certificar que exceções não mapeadas da rede (RuntimeException) encerram a execução
     * retornando Result.Error imediatamente sem realizar leituras no cache.
     */
    @Test
    fun `getUsers should return Error and not attempt cache when generic exception occurs`() = runTest {
        // Arrange
        coEvery { apiService.getUsers() } throws RuntimeException("erro inesperado")

        // Act
        val result = repository.getUsers()

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { userDao.getAll() }
    }

    // ========================================================================
    // TESTES PARA getUserById()
    // ========================================================================

    /**
     * Objetivo: Confirmar que a busca por ID recupera o usuário da API,
     * mapeia para o objeto de domínio e salva a entidade correspondente no cache.
     */
    @Test
    fun `getUserById should return mapped user and save to cache when API call succeeds`() = runTest {
        // Arrange
        val response = fakeUserResponse(1)
        coEvery { apiService.getUserById(1) } returns Response.success(response)

        // Act
        val result = repository.getUserById(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.id)
        coVerify(exactly = 1) { userDao.insertAll(listOf(response.toEntity())) }
    }

    /**
     * Objetivo: Tratar o cenário de borda onde a API retorna HTTP 200 OK porém com corpo nulo,
     * garantindo que o repositório responda com Result.Error e evite a inserção no Room.
     */
    @Test
    fun `getUserById should return Error and not save to cache when API response body is null`() = runTest {
        // Arrange
        coEvery { apiService.getUserById(1) } returns Response.success<UserResponse>(null)

        // Act
        val result = repository.getUserById(1)

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { userDao.insertAll(any()) }
    }

    /**
     * Objetivo: Confirmar que respostas de erro HTTP (ex: 404 Not Found) na busca por ID
     * resultem diretamente em Result.Error.
     */
    @Test
    fun `getUserById should return Error when API returns HTTP error`() = runTest {
        // Arrange
        coEvery { apiService.getUserById(1) } returns httpError(404)

        // Act
        val result = repository.getUserById(1)

        // Assert
        assertTrue(result is Result.Error)
    }

    /**
     * Objetivo: Testar o fallback offline na busca por ID. Ao falhar a conexão,
     * o repositório busca o registro correspondente no cache local por ID.
     */
    @Test
    fun `getUserById should fallback to cache when IOException occurs and user exists in cache`() = runTest {
        // Arrange
        coEvery { apiService.getUserById(1) } throws IOException("sem conexão")
        coEvery { userDao.getById(1) } returns fakeUserEntity(1, "Nome em cache")

        // Act
        val result = repository.getUserById(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals("Nome em cache", (result as Result.Success).data.name)
    }

    /**
     * Objetivo: Garantir o retorno de Result.Error caso ocorra falha de rede e o usuário
     * solicitado não esteja disponível no banco local.
     */
    @Test
    fun `getUserById should return Error when IOException occurs and user is absent from cache`() = runTest {
        // Arrange
        coEvery { apiService.getUserById(1) } throws IOException("sem conexão")
        coEvery { userDao.getById(1) } returns null

        // Act
        val result = repository.getUserById(1)

        // Assert
        assertTrue(result is Result.Error)
    }
}