package com.weeklylotto.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    fun QR_수동입력_영역이_열리고_입력과_파싱버튼이_동작한다() {
        composeRule.onNodeWithText("QR 스캔").performClick()

        composeRule.onNodeWithTag("qr_manual_input").performScrollTo()
        composeRule.onNodeWithText("수동 입력 백업").assertIsDisplayed()
        composeRule.onNodeWithTag("qr_manual_input")
            .performTextClearance()
        composeRule.onNodeWithTag("qr_manual_input")
            .performTextInput("https://example.com?drwNo=1100&numbers=3,14,25,31,38,42")
        composeRule.onNodeWithTag("qr_manual_parse").performClick()
    }

    @Test
    fun QR_수동입력_잘못된_URL도_앱이_중단되지_않는다() {
        composeRule.onNodeWithText("QR 스캔").performClick()

        composeRule.onNodeWithTag("qr_manual_input").performScrollTo()
        composeRule.onNodeWithTag("qr_manual_input")
            .performTextClearance()
        composeRule.onNodeWithTag("qr_manual_input")
            .performTextInput("https://example.com?foo=bar")
        composeRule.onNodeWithTag("qr_manual_parse").performClick()
        composeRule.onNodeWithText("수동 입력 백업").assertIsDisplayed()
    }
}
