package com.shg25.limimeshi.core.network.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.shg25.limimeshi.core.model.Campaign
import java.time.Instant

/**
 * Firestore用のキャンペーンデータクラス
 */
data class FirestoreCampaign(
    @DocumentId
    val id: String = "",
    val chainId: String = "",
    val name: String = "",
    val description: String = "",
    val saleStartTime: Timestamp? = null,
    val saleEndTime: Timestamp? = null,
    val xPostUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toModel(): Campaign = Campaign(
        id = id,
        chainId = chainId,
        name = name,
        description = description,
        saleStartTime = saleStartTime?.toDate()?.toInstant() ?: Instant.now(),
        saleEndTime = saleEndTime?.toDate()?.toInstant(),
        xPostUrl = xPostUrl?.takeIf { it.isNotBlank() },
        createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
        updatedAt = updatedAt?.toDate()?.toInstant() ?: Instant.now()
    )
}
