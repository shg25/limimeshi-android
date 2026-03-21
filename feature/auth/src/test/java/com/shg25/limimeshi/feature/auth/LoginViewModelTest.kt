package com.shg25.limimeshi.feature.auth

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.shg25.limimeshi.core.domain.GetCurrentUserUseCase
import com.shg25.limimeshi.core.domain.SignInWithGoogleUseCase
import com.shg25.limimeshi.core.domain.SignOutUseCase
import com.shg25.limimeshi.core.model.AuthUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
@DisplayName("LoginViewModel")
class LoginViewModelTest {

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var googleCredentialProvider: GoogleCredentialProvider

    private val testContext = mockk<android.content.Context>()
    private val testWebClientId = "test-web-client-id"
    private val testIdToken = "test-id-token"
    private val testUser = AuthUser(uid = "uid", displayName = "Test User", email = "test@example.com")

    @BeforeEach
    fun setup() {
        signInWithGoogleUseCase = mockk()
        signOutUseCase = mockk(relaxed = true)
        getCurrentUserUseCase = mockk()
        googleCredentialProvider = mockk()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(
            signInWithGoogleUseCase = signInWithGoogleUseCase,
            signOutUseCase = signOutUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            googleCredentialProvider = googleCredentialProvider
        )
    }

    @Nested
    @DisplayName("init")
    inner class Init {

        @Test
        @DisplayName("ログイン済みの場合、isLoggedIn=trueとuserNameが設定される")
        fun whenLoggedIn_setsLoggedInState() {
            // Given
            every { getCurrentUserUseCase() } returns testUser

            // When
            val viewModel = createViewModel()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.isLoggedIn)
            assertEquals("Test User", state.userName)
        }

        @Test
        @DisplayName("未ログインの場合、isLoggedIn=falseでuserNameがnull")
        fun whenNotLoggedIn_setsNotLoggedInState() {
            // Given
            every { getCurrentUserUseCase() } returns null

            // When
            val viewModel = createViewModel()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoggedIn)
            assertNull(state.userName)
        }
    }

    @Nested
    @DisplayName("signInWithGoogle")
    inner class SignInWithGoogle {

        @Test
        @DisplayName("成功時、isLoggedIn=trueとuserNameが設定される")
        fun whenSuccess_setsLoggedInState() {
            // Given
            every { getCurrentUserUseCase() } returns null
            coEvery { googleCredentialProvider.getGoogleIdToken(testContext, testWebClientId) } returns testIdToken
            coEvery { signInWithGoogleUseCase(testIdToken) } returns Result.success(testUser)

            val viewModel = createViewModel()

            // When
            viewModel.signInWithGoogle(testContext, testWebClientId)

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.isLoggedIn)
            assertEquals("Test User", state.userName)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

        @Test
        @DisplayName("ユーザーがキャンセルした場合、isLoading=falseのみ")
        fun whenCancelled_setsNotLoading() {
            // Given
            every { getCurrentUserUseCase() } returns null
            coEvery {
                googleCredentialProvider.getGoogleIdToken(testContext, testWebClientId)
            } throws GetCredentialCancellationException()

            val viewModel = createViewModel()

            // When
            viewModel.signInWithGoogle(testContext, testWebClientId)

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isLoggedIn)
            assertNull(state.errorMessage)
        }

        @Test
        @DisplayName("NoCredentialException時、エラーメッセージが設定される")
        fun whenNoCredential_setsErrorMessage() {
            // Given
            every { getCurrentUserUseCase() } returns null
            coEvery {
                googleCredentialProvider.getGoogleIdToken(testContext, testWebClientId)
            } throws NoCredentialException()

            val viewModel = createViewModel()

            // When
            viewModel.signInWithGoogle(testContext, testWebClientId)

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Googleアカウントが見つかりません。端末にGoogleアカウントを追加してください。", state.errorMessage)
        }

        @Test
        @DisplayName("Firebase認証失敗時、エラーメッセージが設定される")
        fun whenFirebaseAuthFails_setsErrorMessage() {
            // Given
            every { getCurrentUserUseCase() } returns null
            coEvery { googleCredentialProvider.getGoogleIdToken(testContext, testWebClientId) } returns testIdToken
            coEvery { signInWithGoogleUseCase(testIdToken) } returns Result.failure(RuntimeException("Auth failed"))

            val viewModel = createViewModel()

            // When
            viewModel.signInWithGoogle(testContext, testWebClientId)

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isLoggedIn)
            assertEquals("ログインに失敗しました: Auth failed", state.errorMessage)
        }
    }

    @Nested
    @DisplayName("signOut")
    inner class SignOut {

        @Test
        @DisplayName("signOutUseCase呼び出し + 状態クリア")
        fun callsSignOutUseCaseAndClearsState() {
            // Given
            every { getCurrentUserUseCase() } returns testUser
            val viewModel = createViewModel()

            // When
            viewModel.signOut()

            // Then
            verify { signOutUseCase() }
            val state = viewModel.uiState.value
            assertFalse(state.isLoggedIn)
            assertNull(state.userName)
        }
    }

    @Nested
    @DisplayName("clearError")
    inner class ClearError {

        @Test
        @DisplayName("errorMessageがnullになる")
        fun clearsErrorMessage() {
            // Given
            every { getCurrentUserUseCase() } returns null
            coEvery {
                googleCredentialProvider.getGoogleIdToken(testContext, testWebClientId)
            } throws NoCredentialException()

            val viewModel = createViewModel()
            viewModel.signInWithGoogle(testContext, testWebClientId)

            // When
            viewModel.clearError()

            // Then
            assertNull(viewModel.uiState.value.errorMessage)
        }
    }
}
