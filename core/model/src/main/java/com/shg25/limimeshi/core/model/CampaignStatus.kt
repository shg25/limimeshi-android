package com.shg25.limimeshi.core.model

/**
 * キャンペーンのステータス
 * 販売開始日時と販売終了日時から自動判定
 */
sealed class CampaignStatus {
    /** 販売開始前 */
    data object Upcoming : CampaignStatus()

    /** 販売中（開始からの経過日数を含む） */
    data class Active(val daysElapsed: Int) : CampaignStatus()

    /** 販売中（開始から1ヶ月以上経過） */
    data class ActiveLongTerm(val monthsElapsed: Int) : CampaignStatus()

    /** 販売終了 */
    data object Ended : CampaignStatus()
}
