@file:Suppress("ktlint:standard:filename")

package com.weeklylotto.app.worker

data class ResultReminderMessage(
    val shouldNotify: Boolean,
    val title: String,
    val body: String,
)

fun resolveResultReminderMessage(
    latestRoundNumber: Int?,
    hasTicketsForLatestRound: Boolean,
    lastViewedRound: Int?,
): ResultReminderMessage {
    val defaultMessage =
        ResultReminderMessage(
            shouldNotify = true,
            title = "이번 주 로또 결과 확인",
            body = "저장한 번호의 당첨 결과를 확인해보세요.",
        )
    val dismissedMessage =
        ResultReminderMessage(
            shouldNotify = false,
            title = "",
            body = "",
        )
    val unseenResultMessage =
        latestRoundNumber?.let { roundNumber ->
            ResultReminderMessage(
                shouldNotify = true,
                title = "아직 당첨 결과를 확인하지 않았어요",
                body = "${roundNumber}회 저장 번호의 결과를 지금 확인해보세요.",
            )
        } ?: defaultMessage
    return when {
        latestRoundNumber == null -> defaultMessage
        !hasTicketsForLatestRound -> dismissedMessage
        lastViewedRound != null && lastViewedRound >= latestRoundNumber -> dismissedMessage
        else -> unseenResultMessage
    }
}
