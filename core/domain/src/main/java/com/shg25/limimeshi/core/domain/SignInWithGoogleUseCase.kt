package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.model.AuthUser
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        return try {
            val user = authRepository.signInWithGoogleIdToken(idToken)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
