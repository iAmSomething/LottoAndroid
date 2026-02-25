package com.weeklylotto.app.ui.navigation

import android.net.Uri

private const val APP_SCHEME = "weeklylotto"

fun routeToDeepLink(route: String): Uri = Uri.parse("$APP_SCHEME://$route")

fun deepLinkToRoute(uri: Uri?): String? {
    if (uri == null || uri.scheme != APP_SCHEME) return null

    val candidate = uri.host ?: uri.path?.trim('/')
    return when (candidate) {
        AppDestination.Home.route -> AppDestination.Home.route
        AppDestination.Manage.route -> AppDestination.Manage.route
        AppDestination.Generator.route -> AppDestination.Generator.route
        AppDestination.Result.route -> AppDestination.Result.route
        AppDestination.Stats.route -> AppDestination.Stats.route
        AppDestination.QrScan.route -> AppDestination.QrScan.route
        AppDestination.ManualAdd.route -> AppDestination.ManualAdd.route
        AppDestination.Import.route -> AppDestination.Import.route
        AppDestination.Settings.route -> AppDestination.Settings.route
        else -> null
    }
}
