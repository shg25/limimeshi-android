package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import javax.inject.Inject

/**
 * お気に入り登録・解除をトグルするUseCase
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    /**
     * お気に入り状態をトグル
     * @param chainId チェーン店ID
     * @param currentlyFavorite 現在のお気に入り状態
     * @return 成功した場合はResult.success、失敗した場合はResult.failure
     */
    suspend operator fun invoke(chainId: String, currentlyFavorite: Boolean): Result<Unit> {
        return try {
            favoritesRepository.toggleFavorite(chainId, currentlyFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
