package com.weeklylotto.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", "홈", Icons.Outlined.Home)

    data object Manage : AppDestination("manage", "번호관리", Icons.AutoMirrored.Outlined.ListAlt)

    data object Generator : AppDestination("generator", "번호 생성", Icons.AutoMirrored.Outlined.ManageSearch)

    data object Result : AppDestination("result", "당첨결과", Icons.Outlined.EmojiEvents)

    data object Stats : AppDestination("stats", "통계", Icons.Outlined.Analytics)

    data object QrScan : AppDestination("qr_scan", "QR 스캔", Icons.Outlined.QrCodeScanner)

    data object Settings : AppDestination("settings", "알림설정", Icons.Outlined.Settings)

    data object Reminder : AppDestination("reminder", "알림", Icons.Outlined.Notifications)

    data object ManualAdd : AppDestination("manual_add", "번호 직접 추가", Icons.AutoMirrored.Outlined.ManageSearch)

    data object Import : AppDestination("import", "가져오기", Icons.Outlined.Inventory2)

    data object ManageTicketDetail : AppDestination("manage_detail/{ticketId}", "상세", Icons.Outlined.Inventory2)

    fun detailRoute(ticketId: Long): String = "manage_detail/$ticketId"

    companion object {
        val bottomTabs = listOfNotNull(Home, Manage, Result)
    }
}
