package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.model.Campaign
import com.shg25.limimeshi.core.model.CampaignStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * キャンペーンのステータスを判定するUseCase
 */
class GetCampaignStatusUseCase @Inject constructor() {

    /**
     * キャンペーンのステータスを判定
     * @param campaign キャンペーン
     * @param now 現在時刻（テスト用に注入可能）
     * @return ステータス
     */
    operator fun invoke(campaign: Campaign, now: Instant = Instant.now()): CampaignStatus {
        val saleStartTime = campaign.saleStartTime
        val saleEndTime = campaign.saleEndTime

        // 販売開始前
        if (now.isBefore(saleStartTime)) {
            return CampaignStatus.Upcoming
        }

        // 販売終了後
        if (saleEndTime != null && now.isAfter(saleEndTime)) {
            return CampaignStatus.Ended
        }

        // 販売中 - 経過日数を計算
        val daysElapsed = ChronoUnit.DAYS.between(saleStartTime, now).toInt()

        return if (daysElapsed >= 30) {
            val monthsElapsed = daysElapsed / 30
            CampaignStatus.ActiveLongTerm(monthsElapsed)
        } else {
            CampaignStatus.Active(daysElapsed)
        }
    }
}
