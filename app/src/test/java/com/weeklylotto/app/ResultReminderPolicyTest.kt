package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.worker.resolveResultReminderMessage
import org.junit.Test

class ResultReminderPolicyTest {
    @Test
    fun 최신회차_티켓이_있고_미확인상태면_리마인드를_보낸다() {
        val decision =
            resolveResultReminderMessage(
                latestRoundNumber = 1212,
                hasTicketsForLatestRound = true,
                lastViewedRound = 1211,
            )

        assertThat(decision.shouldNotify).isTrue()
        assertThat(decision.title).isEqualTo("아직 당첨 결과를 확인하지 않았어요")
    }

    @Test
    fun 최신회차_티켓이_없으면_리마인드를_건너뛴다() {
        val decision =
            resolveResultReminderMessage(
                latestRoundNumber = 1212,
                hasTicketsForLatestRound = false,
                lastViewedRound = null,
            )

        assertThat(decision.shouldNotify).isFalse()
    }

    @Test
    fun 최신회차를_이미_확인했으면_리마인드를_건너뛴다() {
        val decision =
            resolveResultReminderMessage(
                latestRoundNumber = 1212,
                hasTicketsForLatestRound = true,
                lastViewedRound = 1212,
            )

        assertThat(decision.shouldNotify).isFalse()
    }
}
