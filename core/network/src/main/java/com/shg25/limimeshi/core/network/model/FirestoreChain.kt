package com.shg25.limimeshi.core.network.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.shg25.limimeshi.core.model.Chain
import java.time.Instant

/**
 * Firestore用のチェーン店データクラス
 */
data class FirestoreChain(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val furigana: String = "",
    val favoriteCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toModel(): Chain = Chain(
        id = id,
        name = name,
        furigana = furigana,
        favoriteCount = favoriteCount,
        createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
        updatedAt = updatedAt?.toDate()?.toInstant() ?: Instant.now()
    )
}
