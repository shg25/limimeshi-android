package com.shg25.limimeshi.core.model

/**
 * チェーン店とそれに紐づくキャンペーンのセット
 * UI表示用の複合データ
 */
data class ChainWithCampaigns(
    /** チェーン店情報 */
    val chain: Chain,

    /** 紐づくキャンペーン一覧（販売開始日時の降順） */
    val campaigns: List<Campaign>
) {
    /** 最新キャンペーンの販売開始日時（ソート用） */
    val latestCampaignStartTime = campaigns.firstOrNull()?.saleStartTime
}
