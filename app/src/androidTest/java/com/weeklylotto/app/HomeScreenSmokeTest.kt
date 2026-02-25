package com.weeklylotto.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 앱_기본_탭과_헤더가_노출된다() {
        composeRule.onNodeWithText("매주로또").assertIsDisplayed()
        composeRule.onNodeWithText("홈").assertIsDisplayed()
        composeRule.onNodeWithText("번호관리").assertIsDisplayed()
        composeRule.onNodeWithText("당첨결과").assertIsDisplayed()
    }
}
