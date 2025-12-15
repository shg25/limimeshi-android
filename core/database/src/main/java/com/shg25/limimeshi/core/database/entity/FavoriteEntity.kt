package com.shg25.limimeshi.core.database.entity

import androidx.room.Entity
import com.shg25.limimeshi.core.model.Favorite
import java.time.Instant

/**
 * お気に入りのローカルキャッシュ用Entity
 *
 * ユーザーIDとチェーンIDの複合主キーで管理
 */
@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "chainId"]
)
data class FavoriteEntity(
    /** ユーザーID */
    val userId: String,

    /** チェーン店ID */
    val chainId: String,

    /** 登録日時（エポックミリ秒） */
    val createdAt: Long
) {
    fun toModel(): Favorite = Favorite(
        chainId = chainId,
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    companion object {
        fun fromModel(userId: String, favorite: Favorite): FavoriteEntity = FavoriteEntity(
            userId = userId,
            chainId = favorite.chainId,
            createdAt = favorite.createdAt.toEpochMilli()
        )
    }
}
