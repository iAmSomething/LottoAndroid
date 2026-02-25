package com.weeklylotto.app.data.network

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.domain.error.AppResult
import org.junit.Test

class DrawApiClientTest {
    private val client = DrawApiClient(baseUrl = "https://example.com")

    @Test
    fun `parseOfficialPayload parses success response`() {
        val body =
            """
            {
              "returnValue": "success",
              "drwNo": 1212,
              "drwNoDate": "2026-02-21",
              "drwtNo1": 5,
              "drwtNo2": 8,
              "drwtNo3": 25,
              "drwtNo4": 31,
              "drwtNo5": 41,
              "drwtNo6": 44,
              "bnusNo": 45
            }
            """.trimIndent()

        val result = client.parseOfficialPayload(body, requestedRound = 1212)

        assertThat(result is AppResult.Success).isTrue()
        val payload = (result as AppResult.Success).value
        assertThat(payload.round).isEqualTo(1212)
        assertThat(payload.drawDate).isEqualTo("2026-02-21")
        assertThat(payload.main).containsExactly(5, 8, 25, 31, 41, 44).inOrder()
        assertThat(payload.bonus).isEqualTo(45)
    }

    @Test
    fun `parseOfficialPayload returns failure when returnValue is fail`() {
        val body = """{"returnValue":"fail"}"""

        val result = client.parseOfficialPayload(body, requestedRound = 9999)

        assertThat(result is AppResult.Failure).isTrue()
    }

    @Test
    fun `parseMirrorPayload parses fallback response`() {
        val body =
            """
            {
              "draw_no": 1212,
              "numbers": [5, 8, 25, 31, 41, 44],
              "bonus_no": 45,
              "date": "2026-02-21T00:00:00Z"
            }
            """.trimIndent()

        val result = client.parseMirrorPayload(body, requestedRound = 1212)

        assertThat(result is AppResult.Success).isTrue()
        val payload = (result as AppResult.Success).value
        assertThat(payload.round).isEqualTo(1212)
        assertThat(payload.drawDate).isEqualTo("2026-02-21")
        assertThat(payload.main).containsExactly(5, 8, 25, 31, 41, 44).inOrder()
        assertThat(payload.bonus).isEqualTo(45)
    }

    @Test
    fun `parseMirrorPayload returns failure on round mismatch`() {
        val body =
            """
            {
              "draw_no": 1211,
              "numbers": [1, 2, 3, 4, 5, 6],
              "bonus_no": 7,
              "date": "2026-02-14T00:00:00Z"
            }
            """.trimIndent()

        val result = client.parseMirrorPayload(body, requestedRound = 1212)

        assertThat(result is AppResult.Failure).isTrue()
    }
}
