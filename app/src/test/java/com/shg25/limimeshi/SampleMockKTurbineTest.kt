package com.shg25.limimeshi

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * MockKとTurbineのサンプルテスト
 *
 * このファイルはMVP実装時の参考として使用
 * 実際のテストは各機能実装時に追加
 */
class SampleMockKTurbineTest {

    // サンプルインターフェース（MVP実装時にはRepositoryなどを使用）
    interface SampleRepository {
        fun getData(): String
        suspend fun fetchData(): String
    }

    @Nested
    @DisplayName("MockKの基本的な使い方")
    inner class MockKBasics {

        @Test
        @DisplayName("モックの作成と検証")
        fun `mock creation and verification`() {
            // モック作成
            val repository = mockk<SampleRepository>()

            // スタブ設定（戻り値を定義）
            io.mockk.every { repository.getData() } returns "mocked data"

            // 実行
            val result = repository.getData()

            // 検証
            assertEquals("mocked data", result)
            verify { repository.getData() }
        }

        @Test
        @DisplayName("コルーチン対応のモック")
        fun `coroutine mock with coEvery`() = runTest {
            val repository = mockk<SampleRepository>()

            // suspend関数のスタブ
            coEvery { repository.fetchData() } returns "async data"

            // 実行
            val result = repository.fetchData()

            // 検証
            assertEquals("async data", result)
        }
    }

    @Nested
    @DisplayName("Turbineの基本的な使い方")
    inner class TurbineBasics {

        @Test
        @DisplayName("Flowの値を順番に検証")
        fun `flow emits values in order`() = runTest {
            val flow = flowOf(1, 2, 3)

            flow.test {
                assertEquals(1, awaitItem())
                assertEquals(2, awaitItem())
                assertEquals(3, awaitItem())
                awaitComplete()
            }
        }

        @Test
        @DisplayName("StateFlowの初期値と更新を検証")
        fun `stateflow initial and updated values`() = runTest {
            val stateFlow = MutableStateFlow("initial")

            stateFlow.test {
                // 初期値
                assertEquals("initial", awaitItem())

                // 値を更新
                stateFlow.value = "updated"
                assertEquals("updated", awaitItem())

                // キャンセル（StateFlowは完了しないため）
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
