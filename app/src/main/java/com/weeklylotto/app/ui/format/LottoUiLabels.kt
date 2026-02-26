package com.weeklylotto.app.ui.format

import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.ui.component.BadgeTone

fun TicketSource.toSourceChipLabel(): String =
    when (this) {
        TicketSource.GENERATED -> "자동"
        TicketSource.QR_SCAN -> "QR"
        TicketSource.MANUAL -> "수동"
    }

fun TicketSource.toSourceDisplayLabel(): String =
    when (this) {
        TicketSource.GENERATED -> "번호 생성"
        TicketSource.QR_SCAN -> "QR 스캔"
        TicketSource.MANUAL -> "수동 입력"
    }

fun TicketStatus.toStatusLabel(): String =
    when (this) {
        TicketStatus.PENDING -> "대기"
        TicketStatus.WIN -> "당첨"
        TicketStatus.LOSE -> "낙첨"
        TicketStatus.SAVED -> "보관"
    }

fun TicketStatus.toBadgeTone(): BadgeTone =
    when (this) {
        TicketStatus.WIN -> BadgeTone.Success
        TicketStatus.LOSE -> BadgeTone.Neutral
        TicketStatus.PENDING -> BadgeTone.Accent
        TicketStatus.SAVED -> BadgeTone.Accent
    }

fun GameMode.toModeLabel(): String =
    when (this) {
        GameMode.AUTO -> "자동"
        GameMode.MANUAL -> "수동"
        GameMode.SEMI_AUTO -> "반자동"
    }
