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
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && chains.isEmpty() && errorMessage == null
}
