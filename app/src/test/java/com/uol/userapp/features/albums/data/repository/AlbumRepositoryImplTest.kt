package com.uol.userapp.features.albums.data.repository

import com.uol.userapp.core.data.remote.ApiService
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.data.local.AlbumDao
import com.uol.userapp.features.albums.data.local.AlbumEntity
import com.uol.userapp.features.albums.data.local.PhotoDao
import com.uol.userapp.features.albums.data.local.PhotoEntity
import com.uol.userapp.features.albums.data.mapper.toEntity
import com.uol.userapp.features.albums.data.model.AlbumResponse
import com.uol.userapp.features.albums.data.model.PhotoResponse
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

class AlbumRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var albumDao: AlbumDao
    private lateinit var photoDao: PhotoDao
    private lateinit var repository: AlbumRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        albumDao = mockk(relaxed = true)
        photoDao = mockk(relaxed = true)
        repository = AlbumRepositoryImpl(apiService, albumDao, photoDao)
    }

    private fun <T> httpError(code: Int = 500): Response<T> =
        Response.error(code, "erro".toResponseBody("application/json".toMediaTypeOrNull()))

    private fun fakeAlbumResponse(id: Int = 1, userId: Int = 1) =
        AlbumResponse(userId = userId, id = id, title = "Álbum $id")

    private fun fakeAlbumEntity(id: Int = 1, userId: Int = 1, title: String = "Álbum em cache") =
        AlbumEntity(id = id, userId = userId, title = title)

    private fun fakePhotoResponse(id: Int = 1, albumId: Int = 1) =
        PhotoResponse(albumId = albumId, id = id, title = "Foto $id", url = "u$id", thumbnailUrl = "t$id")

    private fun fakePhotoEntity(id: Int = 1, albumId: Int = 1, title: String = "Foto em cache") =
        PhotoEntity(id = id, albumId = albumId, title = title, url = "u", thumbnailUrl = "t")

    // ========================================================================
    // TESTES PARA getAlbumsByUser()
    // ========================================================================

    /**
     * Objetivo: Confirmar que em uma chamada de rede bem-sucedida, o repositório
     * mapeia e retorna os álbuns corretamente, salvando o resultado no banco local.
     */
    @Test
    fun `getAlbumsByUser should return mapped list and save to cache when API call succeeds`() = runTest {
        // Arrange
        val responses = listOf(fakeAlbumResponse(1), fakeAlbumResponse(2))
        coEvery { apiService.getAlbumsByUser(1) } returns Response.success(responses)

        // Act
        val result = repository.getAlbumsByUser(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        coVerify(exactly = 1) { albumDao.insertAll(responses.toEntity()) }
    }

    /**
     * Objetivo: Validar que quando a API responde com um código de erro HTTP (ex: 500),
     * o repositório encerra retornando Result.Error sem interagir com o cache local.
     */
    @Test
    fun `getAlbumsByUser should return Error and not access cache when API returns HTTP error`() = runTest {
        // Arrange
        coEvery { apiService.getAlbumsByUser(1) } returns httpError(500)

        // Act
        val result = repository.getAlbumsByUser(1)

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { albumDao.getByUserId(any()) }
        coVerify(exactly = 0) { albumDao.insertAll(any()) }
    }

    /**
     * Objetivo: Garantir o fallback offline. Quando ocorre uma falha de conexão (IOException),
     * o repositório recupera os álbuns previamente salvos no banco Room do usuário.
     */
    @Test
    fun `getAlbumsByUser should fallback to cache when IOException occurs and cache is populated`() = runTest {
        // Arrange
        coEvery { apiService.getAlbumsByUser(1) } throws IOException("sem conexão")
        coEvery { albumDao.getByUserId(1) } returns listOf(fakeAlbumEntity(1, 1, "Álbum em cache"))

        // Act
        val result = repository.getAlbumsByUser(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals("Álbum em cache", (result as Result.Success).data.first().title)
    }

    /**
     * Objetivo: Verificar que se a requisição falhar por falta de internet e o cache local
     * estiver totalmente vazio, o repositório retorna Result.Error com a mensagem apropriada.
     */
    @Test
    fun `getAlbumsByUser should return Error when IOException occurs and cache is empty`() = runTest {
        // Arrange
        coEvery { apiService.getAlbumsByUser(1) } throws IOException("sem conexão")
        coEvery { albumDao.getByUserId(1) } returns emptyList()

        // Act
        val result = repository.getAlbumsByUser(1)

        // Assert
        assertTrue(result is Result.Error)
        assertEquals("Falha de conexão. Verifique sua internet.", (result as Result.Error).message)
    }

    /**
     * Objetivo: Certificar que erros genéricos de execução (RuntimeException) não acionem
     * o mecanismo de cache local, propagando diretamente o Result.Error.
     */
    @Test
    fun `getAlbumsByUser should return Error and not attempt cache when generic exception occurs`() = runTest {
        // Arrange
        coEvery { apiService.getAlbumsByUser(1) } throws RuntimeException("erro inesperado")

        // Act
        val result = repository.getAlbumsByUser(1)

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { albumDao.getByUserId(any()) }
    }

    // ========================================================================
    // TESTES PARA getPhotosByAlbum()
    // ========================================================================

    /**
     * Objetivo: Confirmar o caminho feliz ao buscar fotos de um álbum, garantindo o retorno
     * dos dados do domínio e o salvamento em lote no banco local via PhotoDao.
     */
    @Test
    fun `getPhotosByAlbum should return mapped list and save to cache when API call succeeds`() = runTest {
        // Arrange
        val responses = listOf(fakePhotoResponse(1), fakePhotoResponse(2))
        coEvery { apiService.getPhotosByAlbum(1) } returns Response.success(responses)

        // Act
        val result = repository.getPhotosByAlbum(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        coVerify(exactly = 1) { photoDao.insertAll(responses.toEntity()) }
    }

    /**
     * Objetivo: Garantir que erros de resposta HTTP na busca de fotos resultem em Error
     * sem consultar o banco de dados nem gravar novas entidades.
     */
    @Test
    fun `getPhotosByAlbum should return Error and not access cache when API returns HTTP error`() = runTest {
        // Arrange
        coEvery { apiService.getPhotosByAlbum(1) } returns httpError(500)

        // Act
        val result = repository.getPhotosByAlbum(1)

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { photoDao.getByAlbumId(any()) }
        coVerify(exactly = 0) { photoDao.insertAll(any()) }
    }

    /**
     * Objetivo: Testar o fallback offline de fotos. Ao perder a conexão com a rede,
     * o repositório deve consultar e devolver as fotos do álbum gravadas no Room.
     */
    @Test
    fun `getPhotosByAlbum should fallback to cache when IOException occurs and cache is populated`() = runTest {
        // Arrange
        coEvery { apiService.getPhotosByAlbum(1) } throws IOException("sem conexão")
        coEvery { photoDao.getByAlbumId(1) } returns listOf(fakePhotoEntity(1, 1, "Foto em cache"))

        // Act
        val result = repository.getPhotosByAlbum(1)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals("Foto em cache", (result as Result.Success).data.first().title)
    }

    /**
     * Objetivo: Garantir o retorno de Result.Error quando a conexão falha e não existem
     * fotos salvas previamente para aquele álbum no cache local.
     */
    @Test
    fun `getPhotosByAlbum should return Error when IOException occurs and cache is empty`() = runTest {
        // Arrange
        coEvery { apiService.getPhotosByAlbum(1) } throws IOException("sem conexão")
        coEvery { photoDao.getByAlbumId(1) } returns emptyList()

        // Act
        val result = repository.getPhotosByAlbum(1)

        // Assert
        assertTrue(result is Result.Error)
        assertEquals("Falha de conexão. Verifique sua internet.", (result as Result.Error).message)
    }

    /**
     * Objetivo: Validar que exceções inesperadas de código na busca de fotos interrompam
     * o fluxo sem realizar tentativas indesejadas de leitura do banco local.
     */
    @Test
    fun `getPhotosByAlbum should return Error and not attempt cache when generic exception occurs`() = runTest {
        // Arrange
        coEvery { apiService.getPhotosByAlbum(1) } throws RuntimeException("erro inesperado")

        // Act
        val result = repository.getPhotosByAlbum(1)

        // Assert
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { photoDao.getByAlbumId(any()) }
    }
}