package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SignOutUseCase")
class SignOutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignOutUseCase

    @BeforeEach
    fun setup() {
        authRepository = mockk(relaxed = true)
        useCase = SignOutUseCase(authRepository)
    }

    @Test
    @DisplayName("repository.signOut()が呼ばれる")
    fun callsRepositorySignOut() {
        // When
        useCase()

        // Then
        verify { authRepository.signOut() }
    }
}
