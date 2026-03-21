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

@DisplayName("ObserveFavoriteIdsUseCase")
class ObserveFavoriteIdsUseCaseTest {

    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: ObserveFavoriteIdsUseCase

    @BeforeEach
    fun setup() {
        favoritesRepository = mockk()
        useCase = ObserveFavoriteIdsUseCase(favoritesRepository)
    }

    @Test
    @DisplayName("repository.favoriteChainIdsを委譲する")
    fun delegatesToRepositoryFavoriteChainIds() = runTest {
        // Given
        val ids = setOf("chain-1", "chain-2")
        every { favoritesRepository.favoriteChainIds } returns flowOf(ids)

        // When & Then
        useCase().test {
            assertEquals(ids, awaitItem())
            awaitComplete()
        }
    }
}
