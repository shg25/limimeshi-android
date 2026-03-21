package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import app.cash.turbine.test

@DisplayName("ObserveLoginStateUseCase")
class ObserveLoginStateUseCaseTest {

    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: ObserveLoginStateUseCase

    @BeforeEach
    fun setup() {
        favoritesRepository = mockk()
        useCase = ObserveLoginStateUseCase(favoritesRepository)
    }

    @Test
    @DisplayName("repository.isLoggedInを委譲する")
    fun delegatesToRepositoryIsLoggedIn() = runTest {
        // Given
        every { favoritesRepository.isLoggedIn } returns flowOf(true)

        // When & Then
        useCase().test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }
}
