package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import javax.inject.Inject

/**
 * Firestoreからお気に入りを同期するUseCase
 */
class SyncFavoritesUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke() {
        favoritesRepository.syncFromFirestore()
    }
}
