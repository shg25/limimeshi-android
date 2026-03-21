package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke() {
        favoritesRepository.clearLocalCache()
        authRepository.signOut()
    }
}
