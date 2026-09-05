package com.uol.userapp.features.users.presentation.list

import com.uol.userapp.core.domain.util.Result
import com.uol.userapp.features.users.domain.model.User
import com.uol.userapp.features.users.domain.usecase.GetUsersUseCase
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
class UsersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getUsersUseCase: GetUsersUseCase

    @Before
    fun setUp() {
        getUsersUseCase = mockk()
    }

    private fun fakeUser(id: Int, name: String, username: String = "user$id", email: String = "e$id@mail.com") =
        User(
            id = id, name = name, username = username, email = email, phone = "123",
            website = "site.com", address = null, company = null
        )

    // ========================================================================
    // TESTES DE CARREGAMENTO INICIAL (init / loadUsers)
    // ========================================================================

    /**
     * Objetivo: Validar que na inicialização do ViewModel, com o sucesso da chamada,
     * o estado transita para Loading e posteriormente para Success contendo todos os usuários.
     */
    @Test
    fun `init should emit Loading then Success with all users when getUsersUseCase succeeds`() = runTest {
        // Arrange
        val users = listOf(fakeUser(1, "Leanne Graham"), fakeUser(2, "Ervin Howell"))
        coEvery { getUsersUseCase() } returns Result.Success(users)

        // Act
        val viewModel = UsersViewModel(getUsersUseCase)
        val stateBeforeLoad = viewModel.uiState.value
        advanceUntilIdle()
        val stateAfterLoad = viewModel.uiState.value

        // Assert
        assertTrue(stateBeforeLoad is UsersUiState.Loading)
        assertTrue(stateAfterLoad is UsersUiState.Success)
        assertEquals(2, (stateAfterLoad as UsersUiState.Success).users.size)
    }

    /**
     * Objetivo: Confirmar que em caso de falha no UseCase, o estado transita para Error
     * propagando a mensagem amigável contida no Result.
     */
    @Test
    fun `init should emit Loading then Error with Result message when getUsersUseCase fails`() = runTest {
        // Arrange
        coEvery { getUsersUseCase() } returns Result.Error(RuntimeException(), "Falha de conexão. Verifique sua internet.")

        // Act
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UsersUiState.Error)
        assertEquals("Falha de conexão. Verifique sua internet.", (state as UsersUiState.Error).message)
    }

    /**
     * Objetivo: Testar o fallback de mensagem de erro padrão quando o Result.Error
     * retornado pelo UseCase não possui mensagem customizada.
     */
    @Test
    fun `init should emit Error with default fallback message when getUsersUseCase returns null message`() = runTest {
        // Arrange
        coEvery { getUsersUseCase() } returns Result.Error(RuntimeException(), null)

        // Act
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals("Não foi possível carregar os usuários.", (state as UsersUiState.Error).message)
    }

    /**
     * Objetivo: Garantir que o estado transite para Empty caso a busca por usuários
     * na inicialização retorne uma lista sem registros.
     */
    @Test
    fun `init should emit Empty state when getUsersUseCase returns empty list`() = runTest {
        // Arrange
        coEvery { getUsersUseCase() } returns Result.Success(emptyList())

        // Act
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UsersUiState.Empty)
    }

    // ========================================================================
    // TESTES DE FILTRO DE BUSCA (onSearchQueryChanged)
    // ========================================================================

    /**
     * Objetivo: Verificar se a busca filtra corretamente os usuários cadastrados
     * comparando a query com o campo nome do usuário.
     */
    @Test
    fun `onSearchQueryChanged should filter list when query matches user name`() = runTest {
        // Arrange
        val users = listOf(fakeUser(1, "Leanne Graham"), fakeUser(2, "Ervin Howell"))
        coEvery { getUsersUseCase() } returns Result.Success(users)
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChanged("Leanne")
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UsersUiState.Success)
        assertEquals(1, (state as UsersUiState.Success).users.size)
        assertEquals("Leanne Graham", state.users.first().name)
    }

    /**
     * Objetivo: Validar a capacidade de filtragem abrangente verificando correspondência
     * em username e e-mail de forma case-insensitive.
     */
    @Test
    fun `onSearchQueryChanged should filter list when query matches username or email`() = runTest {
        // Arrange
        val users = listOf(fakeUser(1, "Leanne Graham", username = "Bret", email = "Sincere@april.biz"))
        coEvery { getUsersUseCase() } returns Result.Success(users)
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChanged("bret") // case-insensitive, casa com username

        // Assert
        assertTrue(viewModel.uiState.value is UsersUiState.Success)
    }

    /**
     * Objetivo: Confirmar a transição do estado para Empty quando nenhum usuário
     * satisfazer os critérios do termo pesquisado.
     */
    @Test
    fun `onSearchQueryChanged should emit Empty state when query matches no users`() = runTest {
        // Arrange
        val users = listOf(fakeUser(1, "Leanne Graham"))
        coEvery { getUsersUseCase() } returns Result.Success(users)
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChanged("termo que não existe")
        val state = viewModel.uiState.value

        // Assert
        assertTrue(state is UsersUiState.Empty)
    }

    /**
     * Objetivo: Garantir que ao limpar a busca (passando string em branco),
     * a lista completa de usuários seja exibida novamente no estado Success.
     */
    @Test
    fun `onSearchQueryChanged should restore all users when search query is blank`() = runTest {
        // Arrange
        val users = listOf(fakeUser(1, "Leanne Graham"), fakeUser(2, "Ervin Howell"))
        coEvery { getUsersUseCase() } returns Result.Success(users)
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("Leanne")

        // Act
        viewModel.onSearchQueryChanged("")
        val state = viewModel.uiState.value

        // Assert
        assertEquals(2, (state as UsersUiState.Success).users.size)
    }

    /**
     * Objetivo: Verificar se a chamada do método atualiza devidamente
     * o valor emitido pelo StateFlow searchQuery.
     */
    @Test
    fun `onSearchQueryChanged should update searchQuery StateFlow with typed text`() = runTest {
        // Arrange
        coEvery { getUsersUseCase() } returns Result.Success(emptyList())
        val viewModel = UsersViewModel(getUsersUseCase)
        advanceUntilIdle()

        // Act
        viewModel.onSearchQueryChanged("teste")

        // Assert
        assertEquals("teste", viewModel.searchQuery.value)
    }
}