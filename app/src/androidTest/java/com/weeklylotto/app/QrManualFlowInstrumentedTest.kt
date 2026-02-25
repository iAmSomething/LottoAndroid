package com.weeklylotto.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrManualFlowInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun QR_수동입력_정상파싱시_저장확인시트가_노출된다() {
        composeRule.onNodeWithText("QR 스캔").performClick()

        composeRule.onNodeWithText("QR URL")
            .performTextClearance()
        composeRule.onNodeWithText("QR URL")
            .performTextInput("https://example.com?drwNo=1100&numbers=3,14,25,31,38,42")
        composeRule.onNodeWithText("파싱").performClick()

        composeRule.onNodeWithText("등록할까요?").assertIsDisplayed()
        composeRule.onNodeWithText("취소").performClick()
    }

    @Test
    fun QR_수동입력_오류파싱시_실패가이드가_노출된다() {
        composeRule.onNodeWithText("QR 스캔").performClick()

        composeRule.onNodeWithText("QR URL")
            .performTextClearance()
        composeRule.onNodeWithText("QR URL")
            .performTextInput("https://example.com?foo=bar")
        composeRule.onNodeWithText("파싱").performClick()

        composeRule.onNodeWithText("스캔 실패 1회").assertIsDisplayed()
    }
}
