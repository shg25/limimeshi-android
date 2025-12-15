package com.shg25.limimeshi.core.data.repository

import com.shg25.limimeshi.core.database.dao.CampaignDao
import com.shg25.limimeshi.core.database.dao.ChainDao
import com.shg25.limimeshi.core.database.entity.CampaignEntity
import com.shg25.limimeshi.core.database.entity.ChainEntity
import com.shg25.limimeshi.core.model.Chain
import com.shg25.limimeshi.core.model.ChainWithCampaigns
import com.shg25.limimeshi.core.network.datasource.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * チェーン店データのRepository
 * Firestoreからデータを取得し、Roomにキャッシュ
 */
@Singleton
class ChainRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val chainDao: ChainDao,
    private val campaignDao: CampaignDao
) {
    /**
     * チェーン店一覧を取得（キャンペーン含む）
     * Roomキャッシュを返しつつ、バックグラウンドでFirestoreから更新
     */
    fun getChainListWithCampaigns(): Flow<List<ChainWithCampaigns>> {
        val oneYearAgoMillis = Instant.now().minus(365, ChronoUnit.DAYS).toEpochMilli()

        return combine(
            chainDao.getAllByFurigana(),
            campaignDao.getAllRecent(oneYearAgoMillis)
        ) { chains, campaigns ->
            val campaignsByChainId = campaigns.groupBy { it.chainId }

            chains.map { chainEntity ->
                ChainWithCampaigns(
                    chain = chainEntity.toModel(),
                    campaigns = campaignsByChainId[chainEntity.id]
                        ?.map { it.toModel() }
                        ?.sortedByDescending { it.saleStartTime }
                        ?: emptyList()
                )
            }
        }
    }

    /**
     * Firestoreからデータを同期してRoomに保存
     */
    suspend fun syncFromFirestore() {
        val chains = firestoreDataSource.getChains()
        val campaigns = firestoreDataSource.getRecentCampaigns()

        chainDao.replaceAll(chains.map { ChainEntity.fromModel(it) })
        campaignDao.replaceAll(campaigns.map { CampaignEntity.fromModel(it) })
    }

    /**
     * キャッシュが空かどうかを確認
     */
    suspend fun isCacheEmpty(): Boolean {
        return chainDao.getAllByFurigana().first().isEmpty()
    }

    /**
     * お気に入りチェーン店のみを取得
     */
    fun getChainsByIds(chainIds: List<String>): Flow<List<Chain>> {
        return chainDao.getByIds(chainIds).map { entities ->
            entities.map { it.toModel() }
        }
    }
}
