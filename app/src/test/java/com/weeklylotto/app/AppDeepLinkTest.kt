package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.ui.navigation.AppDestination
import com.weeklylotto.app.ui.navigation.deepLinkToRouteString
import com.weeklylotto.app.ui.navigation.routeToDeepLinkString
import org.junit.Test

class AppDeepLinkTest {
    @Test
    fun `route를 open query deep link로 변환한다`() {
        val deepLink = routeToDeepLinkString(AppDestination.QrScan.route)

        assertThat(deepLink).isEqualTo("weeklylotto://open?route=qr_scan")
    }

    @Test
    fun `open query deep link를 route로 변환한다`() {
        val route = deepLinkToRouteString("weeklylotto://open?route=result")

        assertThat(route).isEqualTo(AppDestination.Result.route)
    }

    @Test
    fun `기존 host 기반 deep link도 route로 변환한다`() {
        val route = deepLinkToRouteString("weeklylotto://qr_scan")

        assertThat(route).isEqualTo(AppDestination.QrScan.route)
    }

    @Test
    fun `지원하지 않는 route는 null을 반환한다`() {
        val route = deepLinkToRouteString("weeklylotto://open?route=unknown")

        assertThat(route).isNull()
    }

    @Test
    fun `scheme이 다르면 null을 반환한다`() {
        val route = deepLinkToRouteString("https://open?route=qr_scan")

        assertThat(route).isNull()
    }
}
