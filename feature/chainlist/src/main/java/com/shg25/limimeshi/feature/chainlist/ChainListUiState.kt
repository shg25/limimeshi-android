package com.shg25.limimeshi.feature.chainlist

import com.shg25.limimeshi.core.model.ChainSortOrder
import com.shg25.limimeshi.core.model.ChainWithCampaigns

/**
 * チェーン店一覧画面のUI State
 */
data class ChainListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val chains: List<ChainWithCampaigns> = emptyList(),
    val sortOrder: ChainSortOrder = ChainSortOrder.NEWEST,
    val errorMessage: String? = null,
    /** ログイン状態 */
    val isLoggedIn: Boolean = false,
    /** お気に入りチェーンIDセット */
    val favoriteChainIds: Set<String> = emptySet(),
    /** お気に入り操作中のチェーンIDセット */
    val loadingFavoriteChainIds: Set<String> = emptySet()
) {
    val isEmpty: Boolean
        get() = !isLoading && chains.isEmpty() && errorMessage == null

    /** 指定チェーンがお気に入りかどうか */
    fun isFavorite(chainId: String): Boolean = favoriteChainIds.contains(chainId)

    /** 指定チェーンのお気に入り操作中かどうか */
    fun isLoadingFavorite(chainId: String): Boolean = loadingFavoriteChainIds.contains(chainId)
}
