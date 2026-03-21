package com.shg25.limimeshi.core.data.repository

import com.shg25.limimeshi.core.database.dao.FavoriteDao
import com.shg25.limimeshi.core.database.entity.FavoriteEntity
import com.shg25.limimeshi.core.model.Favorite
import com.shg25.limimeshi.core.network.datasource.FirestoreFavoritesDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * お気に入りデータのRepository
 * Firestoreからデータを取得し、Roomにキャッシュ
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FavoritesRepository @Inject constructor(
    private val firestoreFavoritesDataSource: FirestoreFavoritesDataSource,
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository
) {
    /**
     * お気に入りチェーンIDリストを監視
     * ログイン中はRoomキャッシュを返す
     */
    val favoriteChainIds: Flow<Set<String>> = authRepository.isLoggedIn.flatMapLatest { loggedIn ->
        if (loggedIn) {
            val userId = authRepository.currentUserId
            if (userId != null) {
                favoriteDao.getFavoriteChainIds(userId).map { it.toSet() }
            } else {
                flowOf(emptySet())
            }
        } else {
            flowOf(emptySet())
        }
    }

    /**
     * Firestoreからお気に入りを同期
     */
    suspend fun syncFromFirestore() {
        val userId = authRepository.currentUserId ?: return
        val favorites = firestoreFavoritesDataSource.getFavorites(userId)
        val entities = favorites.map { FavoriteEntity.fromModel(userId, it) }
        favoriteDao.replaceAllByUserId(userId, entities)
    }

    /**
     * お気に入り登録
     */
    suspend fun addFavorite(chainId: String) {
        val userId = authRepository.currentUserId ?: throw IllegalStateException("User not logged in")

        // Firestoreに登録
        firestoreFavoritesDataSource.addFavorite(userId, chainId)

        // ローカルキャッシュに追加
        val favorite = Favorite(chainId = chainId, createdAt = java.time.Instant.now())
        favoriteDao.insert(FavoriteEntity.fromModel(userId, favorite))
    }

    /**
     * お気に入り解除
     */
    suspend fun removeFavorite(chainId: String) {
        val userId = authRepository.currentUserId ?: throw IllegalStateException("User not logged in")

        // Firestoreから削除
        firestoreFavoritesDataSource.removeFavorite(userId, chainId)

        // ローカルキャッシュから削除
        favoriteDao.delete(userId, chainId)
    }

    /**
     * お気に入りをトグル
     */
    suspend fun toggleFavorite(chainId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            removeFavorite(chainId)
        } else {
            addFavorite(chainId)
        }
    }

    /**
     * ログアウト時にローカルキャッシュをクリア
     */
    suspend fun clearLocalCache() {
        val userId = authRepository.currentUserId ?: return
        favoriteDao.deleteAllByUserId(userId)
    }
}
