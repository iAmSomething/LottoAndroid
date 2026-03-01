package com.weeklylotto.app.feature.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val OFFICIAL_PURCHASE_URL = "https://dhlottery.co.kr"
const val PURCHASE_REDIRECT_PREF_NAME = "purchase_redirect_notice"
const val PURCHASE_REDIRECT_NOTICE_SEEN_KEY = "notice_seen"

enum class PurchaseRedirectWindowStatus {
    OPEN,
    CLOSING_SOON,
    CLOSED,
}

data class PurchaseRedirectNotice(
    val status: PurchaseRedirectWindowStatus,
    val message: String,
)

fun openExternalUrl(
    context: Context,
    url: String,
): Boolean =
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }.isSuccess

fun buildPurchaseRedirectNotice(now: LocalDateTime = LocalDateTime.now()): PurchaseRedirectNotice {
    val status =
        when {
            isPurchaseClosed(now) -> PurchaseRedirectWindowStatus.CLOSED
            isPurchaseClosingSoon(now) -> PurchaseRedirectWindowStatus.CLOSING_SOON
            else -> PurchaseRedirectWindowStatus.OPEN
        }
    val nowLabel = now.format(DateTimeFormatter.ofPattern("E HH:mm", Locale.KOREAN))
    val timeMessage =
        when (status) {
            PurchaseRedirectWindowStatus.OPEN ->
                "현재는 구매 가능 시간대입니다. 단, 토요일 20:00 이후에는 구매가 제한될 수 있습니다."
            PurchaseRedirectWindowStatus.CLOSING_SOON ->
                "현재는 마감 임박 시간대입니다. 토요일 20:00 이후에는 구매가 제한될 수 있습니다."
            PurchaseRedirectWindowStatus.CLOSED -> {
                val nextOpenLabel =
                    if (now.dayOfWeek == DayOfWeek.SATURDAY && now.toLocalTime() >= LocalTime.of(20, 0)) {
                        "일요일 06:00"
                    } else {
                        "06:00 이후"
                    }
                "현재는 구매 제한 시간대입니다. $nextOpenLabel 다시 시도해 주세요."
            }
        }
    val message =
        buildString {
            append("구매는 동행복권 공식 홈페이지에서만 가능합니다.\n")
            append("성인 인증이 필요하며, 구매 가능 시간은 공식 정책을 따릅니다.\n")
            append("현재 시각: ")
            append(nowLabel)
            append('\n')
            append(timeMessage)
        }
    return PurchaseRedirectNotice(status = status, message = message)
}

private fun isPurchaseClosingSoon(now: LocalDateTime): Boolean =
    now.dayOfWeek == DayOfWeek.SATURDAY && now.toLocalTime() >= LocalTime.of(19, 0) && now.toLocalTime() < LocalTime.of(20, 0)

private fun isPurchaseClosed(now: LocalDateTime): Boolean {
    val time = now.toLocalTime()
    val nightlyClosed = time < LocalTime.of(6, 0)
    val saturdayClosed = now.dayOfWeek == DayOfWeek.SATURDAY && time >= LocalTime.of(20, 0)
    return nightlyClosed || saturdayClosed
}
