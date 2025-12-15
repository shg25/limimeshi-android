package com.shg25.limimeshi.core.model

import java.time.Instant

/**
 * お気に入り登録情報
 */
data class Favorite(
    /** チェーン店ID */
    val chainId: String,

    /** 登録日時 */
    val createdAt: Instant
)

/**
 * お気に入り状態（UI表示用）
 */
data class FavoriteState(
    /** チェーン店ID */
    val chainId: String,

    /** お気に入り登録済みかどうか */
    val isFavorite: Boolean,

    /** 操作中かどうか */
    val isLoading: Boolean = false
)
