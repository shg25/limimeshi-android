package com.shg25.limimeshi.feature.auth

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shg25.limimeshi.core.domain.GetCurrentUserUseCase
import com.shg25.limimeshi.core.domain.SignInWithGoogleUseCase
import com.shg25.limimeshi.core.domain.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val googleCredentialProvider: GoogleCredentialProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        val user = getCurrentUserUseCase()
        _uiState.update {
            it.copy(
                isLoggedIn = user != null,
                userName = user?.displayName
            )
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val idToken = googleCredentialProvider.getGoogleIdToken(context, webClientId)

                signInWithGoogleUseCase(idToken)
                    .onSuccess { authUser ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userName = authUser.displayName
                            )
                        }
                        Timber.d("Google Sign-In successful: ${authUser.email}")
                    }
                    .onFailure { e ->
                        Timber.e(e, "Firebase sign-in failed")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "ログインに失敗しました: ${e.message}"
                            )
                        }
                    }
            } catch (e: GetCredentialCancellationException) {
                Timber.d(e, "Google Sign-In cancelled by user")
                _uiState.update {
                    it.copy(isLoading = false)
                }
            } catch (e: NoCredentialException) {
                Timber.e(e, "NoCredentialException")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Googleアカウントが見つかりません。端末にGoogleアカウントを追加してください。"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Sign-in failed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Googleログインに失敗しました: ${e.message}"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    userName = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
