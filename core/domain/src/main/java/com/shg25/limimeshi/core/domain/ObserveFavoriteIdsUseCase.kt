package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * お気に入りチェーンIDリストを監視するUseCase
 */
class ObserveFavoriteIdsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(): Flow<Set<String>> = favoritesRepository.favoriteChainIds
}
