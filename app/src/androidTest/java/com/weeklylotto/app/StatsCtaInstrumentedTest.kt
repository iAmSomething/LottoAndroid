package com.weeklylotto.app

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StatsCtaInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 통계_중복도_cta를_누르면_번호생성_화면으로_이동한다() {
        composeRule.activity.runOnUiThread {
            composeRule.activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("weeklylotto://stats"),
                    composeRule.activity,
                    MainActivity::class.java,
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
        waitForNodeWithText("통계")
        seedDuplicateTickets()
        waitForNodeWithTag("stats_open_generator")
        composeRule.onNodeWithTag("stats_open_generator").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("전체 초기화").assertIsDisplayed()
    }

    private fun seedDuplicateTickets() {
        val round = Round(number = 2099, drawDate = LocalDate.of(2030, 1, 5))
        val duplicateNumbers = listOf(4, 9, 13, 22, 31, 44).map(::LottoNumber)

        runBlocking {
            AppGraph.ticketRepository.save(
                TicketBundle(
                    round = round,
                    games =
                        listOf(
                            LottoGame(
                                slot = GameSlot.A,
                                numbers = duplicateNumbers,
                                mode = GameMode.MANUAL,
                            ),
                        ),
                    source = TicketSource.MANUAL,
                ),
            )
            AppGraph.ticketRepository.save(
                TicketBundle(
                    round = round,
                    games =
                        listOf(
                            LottoGame(
                                slot = GameSlot.B,
                                numbers = duplicateNumbers,
                                mode = GameMode.MANUAL,
                            ),
                        ),
                    source = TicketSource.MANUAL,
                ),
            )
        }
    }

    private fun waitForNodeWithTag(
        tag: String,
        timeoutMillis: Long = 12_000,
        intervalMillis: Long = 150,
    ) {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            val found =
                runCatching {
                    composeRule.onNodeWithTag(tag).fetchSemanticsNode()
                    true
                }.getOrDefault(false)
            if (found) return
            Thread.sleep(intervalMillis)
        }
        throw AssertionError("Timed out waiting for tag=$tag")
    }

    private fun waitForNodeWithText(
        text: String,
        timeoutMillis: Long = 10_000,
        intervalMillis: Long = 150,
    ) {
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < timeoutMillis) {
            val found =
                runCatching {
                    composeRule.onNodeWithText(text).fetchSemanticsNode()
                    true
                }.getOrDefault(false)
            if (found) return
            Thread.sleep(intervalMillis)
        }
        throw AssertionError("Timed out waiting for text=$text")
    }
}
