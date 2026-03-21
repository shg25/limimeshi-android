package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SyncFavoritesUseCase")
class SyncFavoritesUseCaseTest {

    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: SyncFavoritesUseCase

    @BeforeEach
    fun setup() {
        favoritesRepository = mockk(relaxed = true)
        useCase = SyncFavoritesUseCase(favoritesRepository)
    }

    @Test
    @DisplayName("repository.syncFromFirestore()が呼ばれる")
    fun callsRepositorySyncFromFirestore() = runTest {
        // When
        useCase()

        // Then
        coVerify { favoritesRepository.syncFromFirestore() }
    }
}
