package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.worker.resolvePurchaseReminderMessage
import org.junit.Test
import java.time.LocalDateTime

class PurchaseReminderPolicyTest {
    @Test
    fun 토요일_18시이전_미등록이면_목표형_알림을_보낸다() {
        val decision =
            resolvePurchaseReminderMessage(
                now = LocalDateTime.of(2026, 3, 7, 17, 30),
                hasCurrentRoundTickets = false,
            )

        assertThat(decision.shouldNotify).isTrue()
        assertThat(decision.title).isEqualTo("오늘 18시 전 번호 등록 목표")
    }

    @Test
    fun 토요일_18시이전_등록완료면_알림을_건너뛴다() {
        val decision =
            resolvePurchaseReminderMessage(
                now = LocalDateTime.of(2026, 3, 7, 17, 0),
                hasCurrentRoundTickets = true,
            )

        assertThat(decision.shouldNotify).isFalse()
    }

    @Test
    fun 토요일_18시이후에는_기본_구매알림을_보낸다() {
        val decision =
            resolvePurchaseReminderMessage(
                now = LocalDateTime.of(2026, 3, 7, 18, 30),
                hasCurrentRoundTickets = false,
            )

        assertThat(decision.shouldNotify).isTrue()
        assertThat(decision.title).isEqualTo("이번 주 로또 구매 시간")
    }
}
