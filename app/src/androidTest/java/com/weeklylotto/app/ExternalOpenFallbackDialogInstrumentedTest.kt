package com.weeklylotto.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weeklylotto.app.ui.component.ExternalOpenFallbackDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExternalOpenFallbackDialogInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun 오류_안내가_1초이내_노출된다() {
        composeRule.setContent {
            ExternalOpenFallbackDialog(
                url = "https://dhlottery.co.kr/common.do?method=main",
                onOpenBrowser = {},
                onCopyLink = {},
                onDismiss = {},
            )
        }

        val startedAt = System.currentTimeMillis()
        composeRule.waitUntil(timeoutMillis = 1_000) {
            composeRule
                .onAllNodesWithText("외부 이동에 실패했습니다")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val elapsed = System.currentTimeMillis() - startedAt
        assertTrue("오류 안내 노출 지연: ${elapsed}ms", elapsed <= 1_000)
    }

    @Test
    fun fallback_액션은_2탭_이내로_도달가능하다() {
        var openBrowserCount = 0
        var copyLinkCount = 0
        composeRule.setContent {
            ExternalOpenFallbackDialog(
                url = "https://dhlottery.co.kr/common.do?method=main",
                onOpenBrowser = { openBrowserCount += 1 },
                onCopyLink = { copyLinkCount += 1 },
                onDismiss = {},
            )
        }

        composeRule.onNodeWithText("기본 브라우저로 열기").performClick()
        assertEquals("브라우저 fallback 액션은 1탭으로 도달해야 합니다.", 1, openBrowserCount)

        composeRule.onNodeWithText("링크 복사").performClick()
        assertEquals("링크 복사 fallback 액션은 2탭 이내로 도달해야 합니다.", 1, copyLinkCount)
    }
}
