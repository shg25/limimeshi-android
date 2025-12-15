package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.ChainRepository
import com.shg25.limimeshi.core.model.ChainSortOrder
import com.shg25.limimeshi.core.model.ChainWithCampaigns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * チェーン店一覧を取得するUseCase
 */
class GetChainListUseCase @Inject constructor(
    private val chainRepository: ChainRepository
) {
    /**
     * チェーン店一覧を取得（指定されたソート順で）
     * @param sortOrder ソート順
     * @return チェーン店一覧（キャンペーン含む）のFlow
     */
    operator fun invoke(sortOrder: ChainSortOrder): Flow<List<ChainWithCampaigns>> {
        return chainRepository.getChainListWithCampaigns().map { chainList ->
            when (sortOrder) {
                ChainSortOrder.NEWEST -> sortByNewest(chainList)
                ChainSortOrder.FURIGANA -> sortByFurigana(chainList)
            }
        }
    }

    /**
     * 新着順でソート
     * 最新キャンペーンの販売開始日時が新しいチェーン店を上に
     */
    private fun sortByNewest(chainList: List<ChainWithCampaigns>): List<ChainWithCampaigns> {
        return chainList.sortedByDescending { chain ->
            chain.latestCampaignStartTime
        }
    }

    /**
     * ふりがな順でソート
     */
    private fun sortByFurigana(chainList: List<ChainWithCampaigns>): List<ChainWithCampaigns> {
        return chainList.sortedBy { it.chain.furigana }
    }
}
