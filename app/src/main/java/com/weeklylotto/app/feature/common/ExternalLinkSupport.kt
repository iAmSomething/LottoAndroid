package com.weeklylotto.app.feature.common

import android.content.Context
import android.content.Intent
import android.net.Uri

const val OFFICIAL_PURCHASE_URL = "https://dhlottery.co.kr"
const val PURCHASE_REDIRECT_PREF_NAME = "purchase_redirect_notice"
const val PURCHASE_REDIRECT_NOTICE_SEEN_KEY = "notice_seen"

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
