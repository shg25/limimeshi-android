package com.shg25.limimeshi.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shg25.limimeshi.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * お気に入りのDAO
 */
@Dao
interface FavoriteDao {
    /**
     * ユーザーのお気に入り一覧を取得
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getByUserId(userId: String): Flow<List<FavoriteEntity>>

    /**
     * ユーザーのお気に入りチェーンIDリストを取得
     */
    @Query("SELECT chainId FROM favorites WHERE userId = :userId")
    fun getFavoriteChainIds(userId: String): Flow<List<String>>

    /**
     * 特定のチェーンがお気に入りかどうか確認
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND chainId = :chainId)")
    suspend fun isFavorite(userId: String, chainId: String): Boolean

    /**
     * お気に入りを追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    /**
     * お気に入りを削除
     */
    @Query("DELETE FROM favorites WHERE userId = :userId AND chainId = :chainId")
    suspend fun delete(userId: String, chainId: String)

    /**
     * ユーザーのお気に入りを全て削除（ログアウト時など）
     */
    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    /**
     * お気に入りを一括で置き換え（同期時）
     */
    @androidx.room.Transaction
    suspend fun replaceAllByUserId(userId: String, favorites: List<FavoriteEntity>) {
        deleteAllByUserId(userId)
        favorites.forEach { insert(it) }
    }
}
