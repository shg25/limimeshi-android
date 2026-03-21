package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.model.AuthUser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SignInWithGoogleUseCase")
class SignInWithGoogleUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInWithGoogleUseCase

    private val testIdToken = "test-id-token"
    private val testUser = AuthUser(uid = "uid", displayName = "User", email = "user@test.com")

    @BeforeEach
    fun setup() {
        authRepository = mockk()
        useCase = SignInWithGoogleUseCase(authRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class Invoke {

        @Test
        @DisplayName("成功時、Result.successを返す")
        fun whenSuccess_returnsResultSuccess() = runTest {
            // Given
            coEvery { authRepository.signInWithGoogleIdToken(testIdToken) } returns testUser

            // When
            val result = useCase(testIdToken)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(testUser, result.getOrNull())
        }

        @Test
        @DisplayName("失敗時、Result.failureを返す")
        fun whenFailure_returnsResultFailure() = runTest {
            // Given
            val exception = RuntimeException("Auth failed")
            coEvery { authRepository.signInWithGoogleIdToken(testIdToken) } throws exception

            // When
            val result = useCase(testIdToken)

            // Then
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
    }
}
