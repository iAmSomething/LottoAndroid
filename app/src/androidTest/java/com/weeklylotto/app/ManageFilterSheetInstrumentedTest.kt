package com.weeklylotto.app

import androidx.compose.ui.test.assertIsDisplayed
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
class ManageFilterSheetInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 번호관리_필터_시트가_1초이내_노출된다() {
        openManageFilterSheet()

        val startedAt = System.currentTimeMillis()
        composeRule.waitUntil(timeoutMillis = 1_000) {
            composeRule
                .onAllNodesWithText("직접 입력 범위 적용")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val elapsed = System.currentTimeMillis() - startedAt
        assertTrue("필터 시트 노출 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    @Test
    fun 번호관리_필터_시트_적용이_1초이내_완료된다() {
        openManageFilterSheet()
        composeRule.onNodeWithText("적용").performClick()

        val startedAt = System.currentTimeMillis()
        composeRule.waitUntil(timeoutMillis = 1_000) {
            composeRule
                .onAllNodesWithText("직접 입력 범위 적용")
                .fetchSemanticsNodes()
                .isEmpty()
        }
        val elapsed = System.currentTimeMillis() - startedAt
        assertTrue("필터 시트 닫힘 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    private fun openManageFilterSheet() {
        composeRule.onNodeWithText("번호관리").performClick()
        composeRule.onNodeWithText("필터").performClick()
        composeRule.onNodeWithText("직접 입력 범위 적용").assertIsDisplayed()
    }
}
