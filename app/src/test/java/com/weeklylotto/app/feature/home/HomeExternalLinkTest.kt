package com.weeklylotto.app.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class HomeExternalLinkTest {
    @Test
    fun `buildLottoStoreSearchUrl uses default query`() {
        val expectedQuery = URLEncoder.encode("로또 판매점", StandardCharsets.UTF_8)
        val expectedUrl = "https://www.google.com/maps/search/?api=1&query=$expectedQuery"

        assertEquals(expectedUrl, buildLottoStoreSearchUrl())
    }

    @Test
    fun `buildLottoStoreSearchUrl encodes custom query`() {
        val customQuery = "서울 로또 판매점"
        val expectedQuery = URLEncoder.encode(customQuery, StandardCharsets.UTF_8)
        val expectedUrl = "https://www.google.com/maps/search/?api=1&query=$expectedQuery"

        assertEquals(expectedUrl, buildLottoStoreSearchUrl(customQuery))
    }
}
