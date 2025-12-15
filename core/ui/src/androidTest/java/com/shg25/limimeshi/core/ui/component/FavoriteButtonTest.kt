package com.shg25.limimeshi.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * FavoriteButtonコンポーネントのUIテスト
 *
 * テストケース:
 * - ログインユーザーのボタン有効化
 * - 未ログインユーザーのボタン無効化
 * - 登録・解除状態の表示切り替え
 * - ローディング状態
 */
class FavoriteButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // region ボタンの有効/無効状態

    @Test
    fun favoriteButton_whenEnabled_isClickable() {
        var clicked = false

        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = false,
                enabled = true,
                onClick = { clicked = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsEnabled()
            .performClick()

        assertEquals(true, clicked)
    }

    @Test
    fun favoriteButton_whenDisabled_isNotClickable() {
        var clicked = false

        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = false,
                enabled = false,
                onClick = { clicked = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsNotEnabled()
            .performClick()

        assertEquals(false, clicked)
    }

    // endregion

    // region お気に入り状態の表示

    @Test
    fun favoriteButton_whenFavorite_showsRemoveDescription() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = true,
                isLoading = false,
                enabled = true,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り解除")
            .assertIsDisplayed()
    }

    @Test
    fun favoriteButton_whenNotFavorite_showsAddDescription() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = false,
                enabled = true,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsDisplayed()
    }

    // endregion

    // region ローディング状態

    @Test
    fun favoriteButton_whenLoading_showsLoadingDescription() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = true,
                enabled = true,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り処理中")
            .assertIsDisplayed()
    }

    @Test
    fun favoriteButton_whenLoading_isNotClickable() {
        var clicked = false

        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = true,
                enabled = true,
                onClick = { clicked = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り処理中")
            .assertIsNotEnabled()
            .performClick()

        assertEquals(false, clicked)
    }

    // endregion
}
