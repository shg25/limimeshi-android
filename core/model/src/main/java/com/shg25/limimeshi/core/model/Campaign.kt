package com.shg25.limimeshi.core.model

import java.time.Instant

/**
 * キャンペーン情報
 */
data class Campaign(
    /** キャンペーンID */
    val id: String,

    /** 紐づくチェーン店ID */
    val chainId: String,

    /** キャンペーン名 */
    val name: String,

    /** 説明 */
    val description: String,

    /** 販売開始日時 */
    val saleStartTime: Instant,

    /** 販売終了日時（未設定の場合はnull） */
    val saleEndTime: Instant?,

    /** X Post URL（未設定の場合はnull） */
    val xPostUrl: String?,

    /** 作成日時 */
    val createdAt: Instant,

    /** 更新日時 */
    val updatedAt: Instant
)
