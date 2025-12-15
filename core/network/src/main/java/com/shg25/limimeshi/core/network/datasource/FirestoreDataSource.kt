package com.shg25.limimeshi.core.network.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shg25.limimeshi.core.model.Campaign
import com.shg25.limimeshi.core.model.Chain
import com.shg25.limimeshi.core.network.model.FirestoreCampaign
import com.shg25.limimeshi.core.network.model.FirestoreChain
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore データソース
 * チェーン店とキャンペーンデータの読み取りを担当
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * 全チェーン店を取得
     */
    suspend fun getChains(): List<Chain> {
        val snapshot = firestore.collection(COLLECTION_CHAINS)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreChain::class.java)?.toModel()
        }
    }

    /**
     * 1年以内のキャンペーンを取得
     */
    suspend fun getRecentCampaigns(): List<Campaign> {
        val oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS)
        val oneYearAgoTimestamp = Timestamp(Date.from(oneYearAgo))

        val snapshot = firestore.collection(COLLECTION_CAMPAIGNS)
            .whereGreaterThanOrEqualTo(FIELD_SALE_START_TIME, oneYearAgoTimestamp)
            .orderBy(FIELD_SALE_START_TIME, Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreCampaign::class.java)?.toModel()
        }
    }

    /**
     * 特定チェーン店のキャンペーンを取得（1年以内）
     */
    suspend fun getCampaignsByChainId(chainId: String): List<Campaign> {
        val oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS)
        val oneYearAgoTimestamp = Timestamp(Date.from(oneYearAgo))

        val snapshot = firestore.collection(COLLECTION_CAMPAIGNS)
            .whereEqualTo(FIELD_CHAIN_ID, chainId)
            .whereGreaterThanOrEqualTo(FIELD_SALE_START_TIME, oneYearAgoTimestamp)
            .orderBy(FIELD_SALE_START_TIME, Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreCampaign::class.java)?.toModel()
        }
    }

    companion object {
        private const val COLLECTION_CHAINS = "chains"
        private const val COLLECTION_CAMPAIGNS = "campaigns"
        private const val FIELD_CHAIN_ID = "chainId"
        private const val FIELD_SALE_START_TIME = "saleStartTime"
    }
}
