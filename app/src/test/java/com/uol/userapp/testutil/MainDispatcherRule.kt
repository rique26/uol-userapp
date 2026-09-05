package com.uol.userapp.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Substitui Dispatchers.Main (usado por viewModelScope) por um TestDispatcher
 * controlável durante os testes. StandardTestDispatcher (não Unconfined) é
 * proposital: as corrotinas só rodam quando chamamos advanceUntilIdle(),
 * o que permite capturar o estado "Loading" ANTES da chamada assíncrona
 * terminar — essencial pra testar a sequência de estados do StateFlow.
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}