package com.weeklylotto.app.feature.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class PurchaseRedirectNoticeTest {
    @Test
    fun 평일낮에는_open_상태를_반환한다() {
        val notice = buildPurchaseRedirectNotice(LocalDateTime.of(2026, 3, 2, 10, 30))

        assertThat(notice.status).isEqualTo(PurchaseRedirectWindowStatus.OPEN)
        assertThat(notice.message).contains("구매 가능 시간대")
    }

    @Test
    fun 토요일_마감직전에는_closingSoon_상태를_반환한다() {
        val notice = buildPurchaseRedirectNotice(LocalDateTime.of(2026, 3, 7, 19, 15))

        assertThat(notice.status).isEqualTo(PurchaseRedirectWindowStatus.CLOSING_SOON)
        assertThat(notice.message).contains("마감 임박")
    }

    @Test
    fun 토요일_20시이후에는_closed_상태를_반환한다() {
        val notice = buildPurchaseRedirectNotice(LocalDateTime.of(2026, 3, 7, 21, 0))

        assertThat(notice.status).isEqualTo(PurchaseRedirectWindowStatus.CLOSED)
        assertThat(notice.message).contains("일요일 06:00")
    }

    @Test
    fun 새벽시간에는_closed_상태를_반환한다() {
        val notice = buildPurchaseRedirectNotice(LocalDateTime.of(2026, 3, 3, 2, 20))

        assertThat(notice.status).isEqualTo(PurchaseRedirectWindowStatus.CLOSED)
        assertThat(notice.message).contains("06:00 이후")
    }
}
