package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.model.AuthUser
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetCurrentUserUseCase")
class GetCurrentUserUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: GetCurrentUserUseCase

    private val testUser = AuthUser(uid = "uid", displayName = "User", email = "user@test.com")

    @BeforeEach
    fun setup() {
        authRepository = mockk()
        useCase = GetCurrentUserUseCase(authRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class Invoke {

        @Test
        @DisplayName("ログイン時、AuthUserを返す")
        fun whenLoggedIn_returnsAuthUser() {
            // Given
            every { authRepository.currentUser } returns testUser

            // When
            val result = useCase()

            // Then
            assertEquals(testUser, result)
        }

        @Test
        @DisplayName("未ログイン時、nullを返す")
        fun whenNotLoggedIn_returnsNull() {
            // Given
            every { authRepository.currentUser } returns null

            // When
            val result = useCase()

            // Then
            assertNull(result)
        }
    }
}
