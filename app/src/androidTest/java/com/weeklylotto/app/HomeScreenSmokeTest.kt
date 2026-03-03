package com.weeklylotto.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 앱_기본_탭과_헤더가_노출된다() {
        assertTrue(composeRule.onAllNodesWithText("매주로또").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("홈").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("번호관리").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("당첨결과").fetchSemanticsNodes().isNotEmpty())
    }
}
