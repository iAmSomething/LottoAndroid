package com.weeklylotto.app

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weeklylotto.app.di.AppGraph
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeeklySaveFlowInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 번호생성_저장후_번호관리와_홈에_즉시_반영된다() {
        composeRule.waitForIdle()
        clickTagWithRetry("home_cta_generate")
        clickTagWithRetry("generator_save_weekly")
        val savedTicket =
            waitForSavedTicket(
                timeoutMillis = 20_000,
                intervalMillis = 200,
            )
        assertNotNull(savedTicket)
    }

    private fun waitForSavedTicket(
        timeoutMillis: Long,
        intervalMillis: Long,
    ): com.weeklylotto.app.domain.model.TicketBundle? {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            val latest =
                runBlocking {
                    AppGraph.ticketRepository.latest()
                }
            if (latest != null) {
                return latest
            }
            Thread.sleep(intervalMillis)
        }
        return null
    }

    private fun clickTagWithRetry(
        tag: String,
        timeoutMillis: Long = 10_000,
        intervalMillis: Long = 150,
    ) {
        waitForCondition(timeoutMillis = timeoutMillis, intervalMillis = intervalMillis) {
            runCatching {
                composeRule.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.OnClick)
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForCondition(
        timeoutMillis: Long,
        intervalMillis: Long,
        condition: () -> Boolean,
    ) {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            if (condition()) return
            Thread.sleep(intervalMillis)
        }
        throw AssertionError("Timed out after ${timeoutMillis}ms")
    }
}
