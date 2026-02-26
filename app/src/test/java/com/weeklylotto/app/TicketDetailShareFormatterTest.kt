package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.feature.manage.buildTicketShareText
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TicketDetailShareFormatterTest {
    @Test
    fun 공유텍스트는_회차_출처_상태_게임정보를_포함한다() {
        val ticket =
            TicketBundle(
                id = 101L,
                round = Round(number = 1212, drawDate = LocalDate.of(2026, 2, 21)),
                games =
                    listOf(
                        LottoGame(
                            slot = GameSlot.A,
                            numbers = listOf(1, 7, 19, 23, 34, 45).map(::LottoNumber),
                            mode = GameMode.SEMI_AUTO,
                        ),
                    ),
                source = TicketSource.QR_SCAN,
                createdAt = Instant.parse("2026-02-26T01:23:45Z"),
                status = TicketStatus.PENDING,
            )

        val text = buildTicketShareText(ticket, zoneId = ZoneId.of("UTC"))

        assertThat(text).contains("제 1212회")
        assertThat(text).contains("출처: QR 스캔")
        assertThat(text).contains("상태: 대기")
        assertThat(text).contains("A 게임(반자동): 01, 07, 19, 23, 34, 45")
        assertThat(text).contains("등록일: 2026-02-26 01:23")
    }
}
