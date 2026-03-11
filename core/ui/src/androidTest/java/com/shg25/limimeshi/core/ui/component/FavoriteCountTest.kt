package com.shg25.limimeshi.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * FavoriteCountコンポーネントのUIテスト
 *
 * テストケース:
 * - count=0 → 非表示
 * - count=1 → "♥ 1人がお気に入り登録" 表示
 * - count=42 → "♥ 42人がお気に入り登録" 表示
 */
class FavoriteCountTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun favoriteCount_whenZero_isNotDisplayed() {
        composeTestRule.setContent {
            FavoriteCount(count = 0)
        }

        composeTestRule
            .onNodeWithText("お気に入り登録", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun favoriteCount_whenOne_showsCorrectText() {
        composeTestRule.setContent {
            FavoriteCount(count = 1)
        }

        composeTestRule
            .onNodeWithText("♥ 1人がお気に入り登録")
            .assertIsDisplayed()
    }

    @Test
    fun favoriteCount_whenFortyTwo_showsCorrectText() {
        composeTestRule.setContent {
            FavoriteCount(count = 42)
        }

        composeTestRule
            .onNodeWithText("♥ 42人がお気に入り登録")
            .assertIsDisplayed()
    }
}
