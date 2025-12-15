package com.shg25.limimeshi.core.model

import java.time.Instant

/**
 * チェーン店情報
 */
data class Chain(
    /** チェーン店ID */
    val id: String,

    /** チェーン店名 */
    val name: String,

    /** ふりがな（ソート用） */
    val furigana: String,

    /** お気に入り登録数 */
    val favoriteCount: Int,

    /** 作成日時 */
    val createdAt: Instant,

    /** 更新日時 */
    val updatedAt: Instant
)
