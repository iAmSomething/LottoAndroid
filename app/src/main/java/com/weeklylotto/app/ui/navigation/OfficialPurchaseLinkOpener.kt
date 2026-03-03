package com.weeklylotto.app.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object OfficialPurchaseLinkOpener {
    const val OFFICIAL_PURCHASE_URL: String = "https://dhlottery.co.kr"

    fun openWithCustomTab(
        context: Context,
        url: String = OFFICIAL_PURCHASE_URL,
    ): Boolean {
        return try {
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun openWithBrowser(
        context: Context,
        url: String = OFFICIAL_PURCHASE_URL,
    ): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
