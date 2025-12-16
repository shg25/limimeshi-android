package com.shg25.limimeshi.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        val user = firebaseAuth.currentUser
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
                val credentialManager = CredentialManager.create(context)

                // Use GetSignInWithGoogleOption for standard Google Sign-In bottom sheet
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userName = authResult.user?.displayName
                    )
                }

                Timber.d("Google Sign-In successful: ${authResult.user?.email}")

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
            } catch (e: GetCredentialException) {
                Timber.e(e, "GetCredentialException: ${e.type}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Googleログインに失敗しました: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Sign-in failed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "ログインに失敗しました: ${e.message}"
                    )
                }
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userName = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
