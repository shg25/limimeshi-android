package com.shg25.limimeshi.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("AuthRepository")
class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: AuthRepository

    private val testUid = "test-uid"
    private val testDisplayName = "Test User"
    private val testEmail = "test@example.com"
    private val testIdToken = "test-id-token"

    @BeforeEach
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        repository = AuthRepository(firebaseAuth)
    }

    @Nested
    @DisplayName("currentUser")
    inner class CurrentUser {

        @Test
        @DisplayName("ログイン時、AuthUserを返す")
        fun whenLoggedIn_returnsAuthUser() {
            // Given
            val firebaseUser = mockk<FirebaseUser>()
            every { firebaseUser.uid } returns testUid
            every { firebaseUser.displayName } returns testDisplayName
            every { firebaseUser.email } returns testEmail
            every { firebaseAuth.currentUser } returns firebaseUser

            // When
            val result = repository.currentUser

            // Then
            assertEquals(testUid, result?.uid)
            assertEquals(testDisplayName, result?.displayName)
            assertEquals(testEmail, result?.email)
        }

        @Test
        @DisplayName("未ログイン時、nullを返す")
        fun whenNotLoggedIn_returnsNull() {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When
            val result = repository.currentUser

            // Then
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("signInWithGoogleIdToken")
    inner class SignInWithGoogleIdToken {

        @BeforeEach
        fun setupStatic() {
            mockkStatic(GoogleAuthProvider::class)
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        }

        @AfterEach
        fun teardownStatic() {
            unmockkStatic(GoogleAuthProvider::class)
            unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
        }

        @Test
        @DisplayName("成功時、AuthUserを返す")
        fun whenSuccess_returnsAuthUser() = runTest {
            // Given
            val credential = mockk<AuthCredential>()
            every { GoogleAuthProvider.getCredential(testIdToken, null) } returns credential

            val firebaseUser = mockk<FirebaseUser>()
            every { firebaseUser.uid } returns testUid
            every { firebaseUser.displayName } returns testDisplayName
            every { firebaseUser.email } returns testEmail

            val authResult = mockk<AuthResult>()
            every { authResult.user } returns firebaseUser

            val task = mockk<Task<AuthResult>>()
            every { firebaseAuth.signInWithCredential(credential) } returns task
            coEvery { task.await<AuthResult>() } returns authResult

            // When
            val result = repository.signInWithGoogleIdToken(testIdToken)

            // Then
            assertEquals(testUid, result.uid)
            assertEquals(testDisplayName, result.displayName)
            assertEquals(testEmail, result.email)
        }

        @Test
        @DisplayName("Firebase認証失敗時、例外がスローされる")
        fun whenFirebaseAuthFails_throwsException() = runTest {
            // Given
            val credential = mockk<AuthCredential>()
            every { GoogleAuthProvider.getCredential(testIdToken, null) } returns credential

            val task = mockk<Task<AuthResult>>()
            every { firebaseAuth.signInWithCredential(credential) } returns task
            coEvery { task.await<AuthResult>() } throws RuntimeException("Auth failed")

            // When & Then
            assertThrows<RuntimeException> {
                repository.signInWithGoogleIdToken(testIdToken)
            }
        }

        @Test
        @DisplayName("ユーザーがnullの場合、IllegalStateExceptionがスローされる")
        fun whenUserIsNull_throwsIllegalStateException() = runTest {
            // Given
            val credential = mockk<AuthCredential>()
            every { GoogleAuthProvider.getCredential(testIdToken, null) } returns credential

            val authResult = mockk<AuthResult>()
            every { authResult.user } returns null

            val task = mockk<Task<AuthResult>>()
            every { firebaseAuth.signInWithCredential(credential) } returns task
            coEvery { task.await<AuthResult>() } returns authResult

            // When & Then
            assertThrows<IllegalStateException> {
                repository.signInWithGoogleIdToken(testIdToken)
            }
        }
    }

    @Nested
    @DisplayName("signOut")
    inner class SignOut {

        @Test
        @DisplayName("firebaseAuth.signOut()が呼ばれる")
        fun callsFirebaseAuthSignOut() {
            // When
            repository.signOut()

            // Then
            verify { firebaseAuth.signOut() }
        }
    }
}
