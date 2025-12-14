package com.shg25.limimeshi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * JUnit5サンプルテスト
 *
 * JUnit5の主要機能:
 * - @DisplayName: テスト名をカスタマイズ
 * - @Nested: テストをグループ化
 * - Assertions.assertEquals: JUnit5のアサーション
 */
class ExampleUnitTest {

    @Nested
    @DisplayName("基本的な計算テスト")
    inner class BasicCalculations {

        @Test
        @DisplayName("2 + 2 = 4 であること")
        fun `addition is correct`() {
            assertEquals(4, 2 + 2)
        }

        @Test
        @DisplayName("3 * 3 = 9 であること")
        fun `multiplication is correct`() {
            assertEquals(9, 3 * 3)
        }
    }
}
