package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.model.Campaign
import com.shg25.limimeshi.core.model.CampaignStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * GetCampaignStatusUseCaseの単体テスト
 *
 * テストケース:
 * - 予定（Upcoming）: 販売開始前
 * - 開始から◯日経過（Active）: 販売中、30日未満
 * - 開始から◯ヶ月以上経過（ActiveLongTerm）: 販売中、30日以上
 * - 終了（Ended）: 販売終了後
 * - 販売終了日時未設定時の処理
 */
@DisplayName("GetCampaignStatusUseCase")
class GetCampaignStatusUseCaseTest {

    private lateinit var useCase: GetCampaignStatusUseCase

    @BeforeEach
    fun setup() {
        useCase = GetCampaignStatusUseCase()
    }

    @Nested
    @DisplayName("Upcoming（予定）")
    inner class UpcomingTests {

        @Test
        @DisplayName("販売開始前のキャンペーンはUpcomingを返す")
        fun `販売開始前のキャンペーンはUpcomingを返す`() {
            val now = Instant.parse("2024-01-15T00:00:00Z")
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-20T00:00:00Z"),
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertEquals(CampaignStatus.Upcoming, result)
        }

        @Test
        @DisplayName("販売開始直前のキャンペーンはUpcomingを返す")
        fun `販売開始直前のキャンペーンはUpcomingを返す`() {
            val now = Instant.parse("2024-01-19T23:59:59Z")
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-20T00:00:00Z"),
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertEquals(CampaignStatus.Upcoming, result)
        }
    }

    @Nested
    @DisplayName("Active（開始から◯日経過）")
    inner class ActiveTests {

        @Test
        @DisplayName("販売開始当日はActive(0日経過)を返す")
        fun `販売開始当日はActive_0日経過を返す`() {
            val now = Instant.parse("2024-01-20T12:00:00Z")
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-20T00:00:00Z"),
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.Active)
            assertEquals(0, (result as CampaignStatus.Active).daysElapsed)
        }

        @Test
        @DisplayName("販売開始から1日経過でActive(1日経過)を返す")
        fun `販売開始から1日経過でActive_1日経過を返す`() {
            val now = Instant.parse("2024-01-21T12:00:00Z")
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-20T00:00:00Z"),
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.Active)
            assertEquals(1, (result as CampaignStatus.Active).daysElapsed)
        }

        @Test
        @DisplayName("販売開始から29日経過でActive(29日経過)を返す")
        fun `販売開始から29日経過でActive_29日経過を返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(29, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.Active)
            assertEquals(29, (result as CampaignStatus.Active).daysElapsed)
        }
    }

    @Nested
    @DisplayName("ActiveLongTerm（開始から◯ヶ月以上経過）")
    inner class ActiveLongTermTests {

        @Test
        @DisplayName("販売開始から30日経過でActiveLongTerm(1ヶ月)を返す")
        fun `販売開始から30日経過でActiveLongTerm_1ヶ月を返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(30, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.ActiveLongTerm)
            assertEquals(1, (result as CampaignStatus.ActiveLongTerm).monthsElapsed)
        }

        @Test
        @DisplayName("販売開始から60日経過でActiveLongTerm(2ヶ月)を返す")
        fun `販売開始から60日経過でActiveLongTerm_2ヶ月を返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(60, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.ActiveLongTerm)
            assertEquals(2, (result as CampaignStatus.ActiveLongTerm).monthsElapsed)
        }

        @Test
        @DisplayName("販売開始から365日経過でActiveLongTerm(12ヶ月)を返す")
        fun `販売開始から365日経過でActiveLongTerm_12ヶ月を返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(365, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.ActiveLongTerm)
            assertEquals(12, (result as CampaignStatus.ActiveLongTerm).monthsElapsed)
        }
    }

    @Nested
    @DisplayName("Ended（終了）")
    inner class EndedTests {

        @Test
        @DisplayName("販売終了後のキャンペーンはEndedを返す")
        fun `販売終了後のキャンペーンはEndedを返す`() {
            val now = Instant.parse("2024-02-01T00:00:00Z")
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-01T00:00:00Z"),
                saleEndTime = Instant.parse("2024-01-31T23:59:59Z")
            )

            val result = useCase(campaign, now)

            assertEquals(CampaignStatus.Ended, result)
        }

        @Test
        @DisplayName("販売終了直後のキャンペーンはEndedを返す")
        fun `販売終了直後のキャンペーンはEndedを返す`() {
            val saleEndTime = Instant.parse("2024-01-31T23:59:59Z")
            val now = saleEndTime.plusSeconds(1)
            val campaign = createCampaign(
                saleStartTime = Instant.parse("2024-01-01T00:00:00Z"),
                saleEndTime = saleEndTime
            )

            val result = useCase(campaign, now)

            assertEquals(CampaignStatus.Ended, result)
        }
    }

    @Nested
    @DisplayName("販売終了日時未設定")
    inner class NullSaleEndTimeTests {

        @Test
        @DisplayName("販売終了日時未設定で販売中のキャンペーンはActiveを返す")
        fun `販売終了日時未設定で販売中のキャンペーンはActiveを返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(10, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.Active)
            assertEquals(10, (result as CampaignStatus.Active).daysElapsed)
        }

        @Test
        @DisplayName("販売終了日時未設定で長期販売中のキャンペーンはActiveLongTermを返す")
        fun `販売終了日時未設定で長期販売中のキャンペーンはActiveLongTermを返す`() {
            val saleStartTime = Instant.parse("2024-01-20T00:00:00Z")
            val now = saleStartTime.plus(90, ChronoUnit.DAYS)
            val campaign = createCampaign(
                saleStartTime = saleStartTime,
                saleEndTime = null
            )

            val result = useCase(campaign, now)

            assertTrue(result is CampaignStatus.ActiveLongTerm)
            assertEquals(3, (result as CampaignStatus.ActiveLongTerm).monthsElapsed)
        }
    }

    // region ヘルパー関数

    private fun createCampaign(
        saleStartTime: Instant,
        saleEndTime: Instant?
    ): Campaign {
        return Campaign(
            id = "campaign-1",
            chainId = "chain-1",
            name = "テストキャンペーン",
            description = "テスト説明",
            saleStartTime = saleStartTime,
            saleEndTime = saleEndTime,
            xPostUrl = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    // endregion
}
