package com.uol.userapp.features.albums.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.model.Photo
import com.uol.userapp.features.albums.domain.usecase.GetPhotosByAlbumUseCase
import com.uol.userapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getPhotosByAlbumUseCase: GetPhotosByAlbumUseCase

    private fun fakePhoto(id: Int) = Photo(
        id = id,
        albumId = 1,
        title = "Foto $id",
        url = "https://example.com/$id.jpg",
        thumbnailUrl = "https://example.com/${id}_thumb.jpg"
    )

    private fun buildViewModel(): AlbumDetailViewModel = AlbumDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("albumId" to 1)),
        getPhotosByAlbumUseCase = getPhotosByAlbumUseCase
    )

    @Before
    fun setUp() {
        getPhotosByAlbumUseCase = mockk()
    }

    // ========================================================================
    // TESTES DE INICIALIZAÇÃO E CARREGAMENTO DE FOTOS
    // ========================================================================

    /**
     * Objetivo: Validar que na inicialização, ao obter sucesso na busca pelas fotos,
     * o estado transita de Loading para Success com a lista populada.
     */
    @Test
    fun `init should emit Loading then Success with photos when use case succeeds`() = runTest {
        // Arrange
        val photos = listOf(fakePhoto(1), fakePhoto(2))
        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Success(photos)

        // Act
        val viewModel = buildViewModel()
        val stateBeforeLoad = viewModel.uiState.value
        advanceUntilIdle()
        val stateAfterLoad = viewModel.uiState.value

        // Assert
        assertTrue(stateBeforeLoad is AlbumDetailUiState.Loading)
        assertTrue(stateAfterLoad is AlbumDetailUiState.Success)
        assertEquals(2, (stateAfterLoad as AlbumDetailUiState.Success).photos.size)
    }

    /**
     * Objetivo: Validar que se a busca por fotos retornar uma lista vazia,
     * o estado do ViewModel transita diretamente para Empty.
     */
    @Test
    fun `init should emit Empty state when photo list is empty`() = runTest {
        // Arrange
        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Success(emptyList())

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is AlbumDetailUiState.Empty)
    }

    /**
     * Objetivo: Garantir que em caso de erro na busca pelas fotos,
     * o estado transita para Error utilizando a mensagem informada pelo Result.
     */
    @Test
    fun `init should emit Error state with message from result when use case fails`() = runTest {
        // Arrange
        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Error(RuntimeException(), "Álbum não encontrado.")

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is AlbumDetailUiState.Error)
        assertEquals("Álbum não encontrado.", (state as AlbumDetailUiState.Error).message)
    }

    /**
     * Objetivo: Testar a mensagem fallback padrão quando o Result.Error possui mensagem nula.
     */
    @Test
    fun `init should emit Error state with default fallback message when error message is null`() = runTest {
        // Arrange
        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Error(RuntimeException(), null)

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals("Não foi possível carregar as fotos.", (state as AlbumDetailUiState.Error).message)
    }

    // ========================================================================
    // TESTES DE AÇÕES MANUAIS E RETRY (loadPhotos)
    // ========================================================================

    /**
     * Objetivo: Simular o fluxo do botão de retry. Após uma falha inicial,
     * a chamada manual a loadPhotos() deve reprocessar os dados e alterar o estado para Success.
     */
    @Test
    fun `loadPhotos should recover and emit Success when invoked manually after initial error`() = runTest {
        // Arrange: simula o fluxo do botão de retry na tela
        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Error(RuntimeException(), "Falha de conexão.")
        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AlbumDetailUiState.Error)

        coEvery { getPhotosByAlbumUseCase(1) } returns Result.Success(listOf(fakePhoto(1)))

        // Act
        viewModel.loadPhotos()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is AlbumDetailUiState.Success)
    }
}