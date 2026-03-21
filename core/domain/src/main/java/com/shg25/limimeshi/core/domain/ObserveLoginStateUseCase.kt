package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ログイン状態を監視するUseCase
 */
class ObserveLoginStateUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(): Flow<Boolean> = favoritesRepository.isLoggedIn
}
