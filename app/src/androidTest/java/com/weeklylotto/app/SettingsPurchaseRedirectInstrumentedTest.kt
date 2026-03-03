package com.weeklylotto.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPurchaseRedirectInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 설정_화면에_공식구매_CTA가_노출된다() {
        composeRule.onNodeWithText("설정").performClick()
        composeRule.onNodeWithTag("settings_cta_official_purchase").assertIsDisplayed()
    }

    @Test
    fun 설정_공식구매_CTA_탭시_1초이내_안내_다이얼로그가_노출된다() {
        composeRule.onNodeWithText("설정").performClick()
        composeRule.onNodeWithTag("settings_cta_official_purchase").performClick()

        val startedAt = System.currentTimeMillis()
        composeRule.waitUntil(timeoutMillis = 1_000) {
            composeRule
                .onAllNodesWithText("공식 구매 페이지로 이동")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val elapsed = System.currentTimeMillis() - startedAt
        assertTrue("안내 다이얼로그 노출 지연: ${elapsed}ms", elapsed <= 1_000)
        composeRule.onNodeWithText("취소").performClick()
    }
}
