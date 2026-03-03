package com.weeklylotto.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HotpathRenderLatencyInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 홈_첫_렌더가_1초이내_노출된다() {
        val elapsed = waitForTextWithin("매주로또", timeoutMillis = 1_000)
        assertTrue("홈 첫 렌더 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    @Test
    fun 번호관리_탭_전환_렌더가_1초이내_완료된다() {
        composeRule.onNodeWithText("번호관리").performClick()
        val elapsed = waitForTextWithin("번호관리", timeoutMillis = 1_000)
        assertTrue("번호관리 렌더 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    @Test
    fun 당첨결과_탭_전환_렌더가_1초이내_완료된다() {
        composeRule.onNodeWithText("당첨결과").performClick()
        val elapsed = waitForTextWithin("당첨 결과", timeoutMillis = 1_000)
        assertTrue("당첨결과 렌더 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    private fun waitForTextWithin(
        text: String,
        timeoutMillis: Long,
        intervalMillis: Long = 100,
    ): Long {
        val startedAt = System.currentTimeMillis()
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        return System.currentTimeMillis() - startedAt
    }
}
