package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.qr.QrTicketParser
import com.weeklylotto.app.domain.error.AppResult
import org.junit.Test

class QrTicketParserTest {
    private val parser = QrTicketParser()

    @Test
    fun `query numbers 형식을 파싱한다`() {
        val input = "https://example.com?drwNo=1100&numbers=3,14,25,31,38,42;7,18,21,35,40,45"

        val result = parser.parse(input)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val parsed = (result as AppResult.Success).value
        assertThat(parsed.round).isEqualTo(1100)
        assertThat(parsed.games).hasSize(2)
        assertThat(parsed.games.first().map { it.value }).containsExactly(3, 14, 25, 31, 38, 42)
    }

    @Test
    fun `compact v 형식을 파싱한다`() {
        val input = "https://example.com?v=1100q031425313842071821354045"

        val result = parser.parse(input)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val parsed = (result as AppResult.Success).value
        assertThat(parsed.round).isEqualTo(1100)
        assertThat(parsed.games).hasSize(2)
    }

    @Test
    fun `실복권과 유사한 q 구분자 다중 게임 형식을 파싱한다`() {
        val input =
            "https://m.dhlottery.co.kr/?v=1003q010429394345q010429394345q010429394345q010429394345q010429394345"

        val result = parser.parse(input)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val parsed = (result as AppResult.Success).value
        assertThat(parsed.round).isEqualTo(1003)
        assertThat(parsed.games).hasSize(5)
        assertThat(parsed.games.first().map { it.value }).containsExactly(1, 4, 29, 39, 43, 45)
    }

    @Test
    fun `v 파라미터 외 추가 쿼리가 있어도 파싱한다`() {
        val input = "https://m.dhlottery.co.kr/?v=1057q081213192740q081213192738&foo=bar"

        val result = parser.parse(input)

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        val parsed = (result as AppResult.Success).value
        assertThat(parsed.round).isEqualTo(1057)
        assertThat(parsed.games).hasSize(2)
    }

    @Test
    fun `지원하지 않는 형식이면 실패한다`() {
        val input = "https://example.com?foo=bar"

        val result = parser.parse(input)

        assertThat(result).isInstanceOf(AppResult.Failure::class.java)
    }
}
