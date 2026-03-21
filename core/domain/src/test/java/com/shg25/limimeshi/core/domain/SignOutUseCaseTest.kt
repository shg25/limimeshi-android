package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SignOutUseCase")
class SignOutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: SignOutUseCase

    @BeforeEach
    fun setup() {
        authRepository = mockk(relaxed = true)
        favoritesRepository = mockk(relaxed = true)
        useCase = SignOutUseCase(authRepository, favoritesRepository)
    }

    @Test
    @DisplayName("ローカルキャッシュをクリアしてからsignOutが呼ばれる")
    fun clearsLocalCacheThenSignsOut() = runTest {
        // When
        useCase()

        // Then
        coVerify { favoritesRepository.clearLocalCache() }
        verify { authRepository.signOut() }
    }
}
