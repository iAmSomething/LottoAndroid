package com.weeklylotto.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeeklySaveFlowInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 번호생성_저장후_번호관리와_홈에_즉시_반영된다() {
        composeRule.onNodeWithText("번호 생성").performClick()
        composeRule.onNodeWithText("이번 주 번호로 저장하기").performClick()
        pressBack()

        composeRule.onNodeWithText("번호관리").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("회 자동", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("저장된 번호가 없습니다.").assertCountEquals(0)
        composeRule.onNodeWithText("회 자동", substring = true).assertIsDisplayed()

        composeRule.onNodeWithText("홈").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("게임 (자동)", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("게임 (자동)", substring = true).assertIsDisplayed()
    }
}
