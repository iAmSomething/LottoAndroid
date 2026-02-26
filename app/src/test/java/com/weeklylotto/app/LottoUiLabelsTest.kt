package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.ui.component.BadgeTone
import com.weeklylotto.app.ui.format.toBadgeTone
import com.weeklylotto.app.ui.format.toModeLabel
import com.weeklylotto.app.ui.format.toSourceChipLabel
import com.weeklylotto.app.ui.format.toSourceDisplayLabel
import com.weeklylotto.app.ui.format.toStatusLabel
import org.junit.Test

class LottoUiLabelsTest {
    @Test
    fun 티켓출처_칩라벨과_상세라벨을_올바르게_매핑한다() {
        assertThat(TicketSource.GENERATED.toSourceChipLabel()).isEqualTo("자동")
        assertThat(TicketSource.QR_SCAN.toSourceChipLabel()).isEqualTo("QR")
        assertThat(TicketSource.MANUAL.toSourceChipLabel()).isEqualTo("수동")

        assertThat(TicketSource.GENERATED.toSourceDisplayLabel()).isEqualTo("번호 생성")
        assertThat(TicketSource.QR_SCAN.toSourceDisplayLabel()).isEqualTo("QR 스캔")
        assertThat(TicketSource.MANUAL.toSourceDisplayLabel()).isEqualTo("수동 입력")
    }

    @Test
    fun 상태라벨과_배지톤을_올바르게_매핑한다() {
        assertThat(TicketStatus.PENDING.toStatusLabel()).isEqualTo("대기")
        assertThat(TicketStatus.WIN.toStatusLabel()).isEqualTo("당첨")
        assertThat(TicketStatus.LOSE.toStatusLabel()).isEqualTo("낙첨")

        assertThat(TicketStatus.PENDING.toBadgeTone()).isEqualTo(BadgeTone.Accent)
        assertThat(TicketStatus.WIN.toBadgeTone()).isEqualTo(BadgeTone.Success)
        assertThat(TicketStatus.LOSE.toBadgeTone()).isEqualTo(BadgeTone.Neutral)
    }

    @Test
    fun 게임모드를_한국어_문구로_매핑한다() {
        assertThat(GameMode.AUTO.toModeLabel()).isEqualTo("자동")
        assertThat(GameMode.MANUAL.toModeLabel()).isEqualTo("수동")
        assertThat(GameMode.SEMI_AUTO.toModeLabel()).isEqualTo("반자동")
    }
}
