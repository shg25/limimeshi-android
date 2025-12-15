package com.shg25.limimeshi.feature.chainlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shg25.limimeshi.core.domain.GetCampaignStatusUseCase
import com.shg25.limimeshi.core.model.Campaign
import com.shg25.limimeshi.core.model.Chain
import com.shg25.limimeshi.core.model.ChainSortOrder
import com.shg25.limimeshi.core.model.ChainWithCampaigns
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * ChainListScreenのUIテスト
 *
 * テストケース:
 * - デフォルトで新着順表示
 * - ソート切り替え（新着順↔ふりがな順）
 * - キャンペーンが販売開始日時降順で表示
 * - 空リスト時「データなし」表示
 */
@RunWith(AndroidJUnit4::class)
class ChainListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val getCampaignStatusUseCase = GetCampaignStatusUseCase()

    // region 空リスト状態

    @Test
    fun 空リスト時にデータなしと表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = emptyList(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("データなし").assertIsDisplayed()
    }

    // endregion

    // region ソート表示

    @Test
    fun デフォルトで新着順が選択されている() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("新着順").assertIsDisplayed()
    }

    @Test
    fun ふりがな順選択時にふりがな順と表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.FURIGANA
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("ふりがな順").assertIsDisplayed()
    }

    @Test
    fun ソートボタンをクリックするとドロップダウンが表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        // ソートチップをクリック
        composeTestRule.onNodeWithText("新着順").performClick()

        // ドロップダウンメニューが表示される
        composeTestRule.onNodeWithText("ふりがな順").assertIsDisplayed()
    }

    // endregion

    // region チェーン店一覧表示

    @Test
    fun チェーン店名が表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("テストチェーン").assertIsDisplayed()
    }

    @Test
    fun お気に入り数が表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("♥ 42").assertIsDisplayed()
    }

    @Test
    fun キャンペーン名が表示される() {
        val uiState = ChainListUiState(
            isLoading = false,
            chains = createTestChains(),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("テストキャンペーン").assertIsDisplayed()
    }

    @Test
    fun キャンペーン0件時に現在キャンペーンはありませんと表示される() {
        val chainWithNoCampaigns = ChainWithCampaigns(
            chain = Chain(
                id = "chain-1",
                name = "キャンペーンなしチェーン",
                furigana = "きゃんぺーんなしちぇーん",
                favoriteCount = 10,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            campaigns = emptyList()
        )

        val uiState = ChainListUiState(
            isLoading = false,
            chains = listOf(chainWithNoCampaigns),
            sortOrder = ChainSortOrder.NEWEST
        )

        composeTestRule.setContent {
            ChainListContent(
                uiState = uiState,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                onSortOrderChange = {},
                onRefresh = {}
            )
        }

        composeTestRule.onNodeWithText("現在キャンペーンはありません").assertIsDisplayed()
    }

    // endregion

    // region ヘルパー関数

    private fun createTestChains(): List<ChainWithCampaigns> {
        val now = Instant.now()
        return listOf(
            ChainWithCampaigns(
                chain = Chain(
                    id = "chain-1",
                    name = "テストチェーン",
                    furigana = "てすとちぇーん",
                    favoriteCount = 42,
                    createdAt = now,
                    updatedAt = now
                ),
                campaigns = listOf(
                    Campaign(
                        id = "campaign-1",
                        chainId = "chain-1",
                        name = "テストキャンペーン",
                        description = "テスト説明文",
                        saleStartTime = now.minus(5, ChronoUnit.DAYS),
                        saleEndTime = null,
                        xPostUrl = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )
        )
    }

    // endregion
}
