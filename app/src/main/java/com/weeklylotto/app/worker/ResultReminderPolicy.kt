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
    if (latestRoundNumber == null) {
        return ResultReminderMessage(
            shouldNotify = true,
            title = "이번 주 로또 결과 확인",
            body = "저장한 번호의 당첨 결과를 확인해보세요.",
        )
    }

    if (!hasTicketsForLatestRound) {
        return ResultReminderMessage(
            shouldNotify = false,
            title = "",
            body = "",
        )
    }

    if (lastViewedRound != null && lastViewedRound >= latestRoundNumber) {
        return ResultReminderMessage(
            shouldNotify = false,
            title = "",
            body = "",
        )
    }

    return ResultReminderMessage(
        shouldNotify = true,
        title = "아직 당첨 결과를 확인하지 않았어요",
        body = "${latestRoundNumber}회 저장 번호의 결과를 지금 확인해보세요.",
    )
}
