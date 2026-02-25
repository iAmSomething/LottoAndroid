package com.weeklylotto.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 하단탭_전환과_생성화면_진입이_동작한다() {
        composeRule.onNodeWithText("번호관리").performClick()
        composeRule.onNodeWithText("저장된 번호").assertIsDisplayed()

        composeRule.onNodeWithText("당첨결과").performClick()
        composeRule.onNodeWithText("당첨 결과").assertIsDisplayed()

        composeRule.onNodeWithText("홈").performClick()
        composeRule.onNodeWithText("매주로또").assertIsDisplayed()

        composeRule.onNodeWithText("번호 생성").performClick()
        composeRule.onNodeWithText("전체 초기화").assertIsDisplayed()
    }
}
