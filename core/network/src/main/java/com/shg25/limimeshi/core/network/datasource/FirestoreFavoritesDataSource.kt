package com.shg25.limimeshi.core.network.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shg25.limimeshi.core.model.Favorite
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestoreお気に入りデータソース
 * お気に入りの登録・解除・取得を担当
 */
@Singleton
class FirestoreFavoritesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * ユーザーのお気に入り一覧を取得
     */
    suspend fun getFavorites(userId: String): List<Favorite> {
        val snapshot = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val chainId = doc.getString(FIELD_CHAIN_ID) ?: doc.id
            val createdAt = doc.getTimestamp(FIELD_CREATED_AT)?.toDate()?.toInstant()
                ?: Instant.now()

            Favorite(chainId = chainId, createdAt = createdAt)
        }
    }

    /**
     * お気に入り登録
     * まずユーザーのお気に入りを登録し、チェーンのカウント更新は別途試行
     */
    suspend fun addFavorite(userId: String, chainId: String) {
        val favoriteRef = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(chainId)

        // お気に入り登録
        favoriteRef.set(
            hashMapOf(
                FIELD_CHAIN_ID to chainId,
                FIELD_CREATED_AT to FieldValue.serverTimestamp()
            )
        ).await()

        // チェーンのカウント更新を試行（失敗しても続行）
        tryUpdateChainFavoriteCount(chainId, increment = 1)
    }

    /**
     * お気に入り解除
     * まずユーザーのお気に入りを削除し、チェーンのカウント更新は別途試行
     */
    suspend fun removeFavorite(userId: String, chainId: String) {
        val favoriteRef = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(chainId)

        // お気に入り削除
        favoriteRef.delete().await()

        // チェーンのカウント更新を試行（失敗しても続行）
        tryUpdateChainFavoriteCount(chainId, increment = -1)
    }

    /**
     * チェーンのお気に入りカウントを更新（ベストエフォート）
     */
    private suspend fun tryUpdateChainFavoriteCount(chainId: String, increment: Long) {
        try {
            val chainRef = firestore.collection(COLLECTION_CHAINS).document(chainId)
            chainRef.update(
                mapOf(
                    FIELD_FAVORITE_COUNT to FieldValue.increment(increment),
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            // チェーンドキュメントが存在しない場合など
            // お気に入り登録自体は成功しているので、ログだけ出力
            Timber.w(e, "Failed to update chain favorite count for $chainId")
        }
    }

    /**
     * 特定チェーンがお気に入りかどうか確認
     */
    suspend fun isFavorite(userId: String, chainId: String): Boolean {
        val snapshot = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(chainId)
            .get()
            .await()

        return snapshot.exists()
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_FAVORITES = "favorites"
        private const val COLLECTION_CHAINS = "chains"
        private const val FIELD_CHAIN_ID = "chainId"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_FAVORITE_COUNT = "favoriteCount"
        private const val FIELD_UPDATED_AT = "updatedAt"
    }
}
