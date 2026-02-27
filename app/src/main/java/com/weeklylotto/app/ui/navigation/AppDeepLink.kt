package com.weeklylotto.app.ui.navigation

import android.net.Uri
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

private const val APP_SCHEME = "weeklylotto"
private const val APP_DEEP_LINK_HOST = "open"

fun routeToDeepLink(route: String): Uri = Uri.parse(routeToDeepLinkString(route))

internal fun routeToDeepLinkString(route: String): String {
    val encodedRoute =
        URLEncoder.encode(
            route.trim(),
            StandardCharsets.UTF_8.name(),
        )
    return "$APP_SCHEME://$APP_DEEP_LINK_HOST?route=$encodedRoute"
}

fun deepLinkToRoute(uri: Uri?): String? = deepLinkToRouteString(uri?.toString())

internal fun deepLinkToRouteString(rawUri: String?): String? {
    if (rawUri.isNullOrBlank()) return null
    val parsed = runCatching { URI(rawUri) }.getOrNull() ?: return null
    if (parsed.scheme != APP_SCHEME) return null

    val queryMap = parseQuery(parsed.rawQuery)
    val candidate =
        if (parsed.host.equals(APP_DEEP_LINK_HOST, ignoreCase = true)) {
            queryMap["route"]
        } else {
            parsed.host ?: parsed.rawAuthority ?: parsed.path?.trim('/')
        }
    return normalizeSupportedRoute(candidate)
}

private fun normalizeSupportedRoute(candidate: String?): String? =
    when (candidate?.trim()?.lowercase(Locale.ROOT)) {
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

private fun parseQuery(rawQuery: String?): Map<String, String> {
    if (rawQuery.isNullOrBlank()) return emptyMap()
    return rawQuery
        .split('&')
        .filter { token -> token.isNotBlank() }
        .associate { token ->
            val parts = token.split('=', limit = 2)
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
            val value = URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8.name())
            key to value
        }
}
