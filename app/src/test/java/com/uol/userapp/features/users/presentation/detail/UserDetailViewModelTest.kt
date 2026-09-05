package com.uol.userapp.features.users.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.albums.domain.model.Album
import com.uol.userapp.features.albums.domain.usecase.GetAlbumsByUserUseCase
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.usecase.GetUserByIdUseCase
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
class UserDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getUserByIdUseCase: GetUserByIdUseCase
    private lateinit var getAlbumsByUserUseCase: GetAlbumsByUserUseCase

    private val fakeUser = User(
        id = 1, name = "Leanne Graham", username = "Bret", email = "e@mail.com",
        phone = "123", website = "site.com", address = null, company = null
    )
    private val fakeAlbums = listOf(Album(id = 1, userId = 1, title = "Álbum 1"))

    private fun buildViewModel(): UserDetailViewModel = UserDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("userId" to 1)),
        getUserByIdUseCase = getUserByIdUseCase,
        getAlbumsByUserUseCase = getAlbumsByUserUseCase
    )

    @Before
    fun setUp() {
        getUserByIdUseCase = mockk()
        getAlbumsByUserUseCase = mockk()
    }

    // ========================================================================
    // TESTES DE INICIALIZAÇÃO E CARREGAMENTO DE DETALHES
    // ========================================================================

    /**
     * Objetivo: Confirmar que na inicialização do ViewModel, quando a busca do usuário
     * e de seus álbuns obtém sucesso, o estado transita de Loading para Success.
     */
    @Test
    fun `init should emit Loading then Success with user and albums when both use cases succeed`() = runTest {
        // Arrange
        coEvery { getUserByIdUseCase(1) } returns Result.Success(fakeUser)
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Success(fakeAlbums)

        // Act
        val viewModel = buildViewModel()
        val stateBeforeLoad = viewModel.uiState.value
        advanceUntilIdle()
        val stateAfterLoad = viewModel.uiState.value

        // Assert
        assertTrue(stateBeforeLoad is UserDetailUiState.Loading)
        assertTrue(stateAfterLoad is UserDetailUiState.Success)
        assertEquals(fakeUser, (stateAfterLoad as UserDetailUiState.Success).user)
        assertEquals(1, stateAfterLoad.albums.size)
    }

    /**
     * Objetivo: Validar que se a busca pelas informações do usuário falhar,
     * o estado é alterado para Error exibindo a mensagem retornada pelo UseCase do usuário.
     */
    @Test
    fun `init should emit Error with user error message when user fetch fails`() = runTest {
        // Arrange
        coEvery { getUserByIdUseCase(1) } returns Result.Error(RuntimeException(), "Usuário não encontrado.")
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Success(fakeAlbums)

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UserDetailUiState.Error)
        assertEquals("Usuário não encontrado.", (state as UserDetailUiState.Error).message)
    }

    /**
     * Objetivo: Verificar que se a busca de álbuns do usuário falhar (mesmo com usuário recuperado),
     * o estado transita para Error exibindo a mensagem de erro específica de álbuns.
     */
    @Test
    fun `init should emit Error with albums error message when albums fetch fails`() = runTest {
        // Arrange
        coEvery { getUserByIdUseCase(1) } returns Result.Success(fakeUser)
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Error(RuntimeException(), "Não foi possível carregar os álbuns.")

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UserDetailUiState.Error)
        assertEquals("Não foi possível carregar os álbuns.", (state as UserDetailUiState.Error).message)
    }

    /**
     * Objetivo: Garantir a precedência de tratamento de erros. Quando ambas as chamadas falham,
     * a mensagem de erro do usuário deve ser mantida com prioridade.
     */
    @Test
    fun `init should prioritize user error message when both user and albums fetch fail`() = runTest {
        // Arrange: o when{} do ViewModel checa userResult antes de albumsResult
        coEvery { getUserByIdUseCase(1) } returns Result.Error(RuntimeException(), "Erro do usuário.")
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Error(RuntimeException(), "Erro dos álbuns.")

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals("Erro do usuário.", (state as UserDetailUiState.Error).message)
    }

    /**
     * Objetivo: Testar a mensagem fallback padrão quando o Result.Error retornado
     * na busca de usuário possui mensagem nula.
     */
    @Test
    fun `init should emit Error with default fallback message when user error message is null`() = runTest {
        // Arrange
        coEvery { getUserByIdUseCase(1) } returns Result.Error(RuntimeException(), null)
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Success(fakeAlbums)

        // Act
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals("Não foi possível carregar o usuário.", (state as UserDetailUiState.Error).message)
    }

    // ========================================================================
    // TESTES DE AÇÕES MANUAIS (loadUserDetail)
    // ========================================================================

    /**
     * Objetivo: Confirmar que a invocação manual do método loadUserDetail() refaz
     * o carregamento completo dos dados de usuário e álbuns com sucesso.
     */
    @Test
    fun `loadUserDetail should reload user and albums data when invoked manually`() = runTest {
        // Arrange
        coEvery { getUserByIdUseCase(1) } returns Result.Success(fakeUser)
        coEvery { getAlbumsByUserUseCase(1) } returns Result.Success(fakeAlbums)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        // Act
        viewModel.loadUserDetail()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value is UserDetailUiState.Success)
    }
}