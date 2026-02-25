package com.weeklylotto.app.data.qr

import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.ParsedTicket
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class QrTicketParser {
    fun parse(rawUrl: String): AppResult<ParsedTicket> {
        return runCatching {
            val uri = URI(rawUrl)
            val queryMap = parseQuery(uri.rawQuery)
            val queryRound = queryMap["drwNo"]?.toIntOrNull()
            val queryNumbers = queryMap["numbers"]

            if (queryRound != null && !queryNumbers.isNullOrBlank()) {
                val games =
                    queryNumbers.split(';')
                        .filter { it.isNotBlank() }
                        .map { gameToken ->
                            gameToken.split(',').map { LottoNumber(it.trim().toInt()) }
                        }
                return@runCatching ParsedTicket(queryRound, games)
            }

            val compact = queryMap["v"]
            if (!compact.isNullOrBlank()) {
                return@runCatching parseCompact(compact)
            }

            error("지원하지 않는 QR URL 형식입니다.")
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.ParseError(it.message ?: "QR 파싱 실패")) },
        )
    }

    private fun parseCompact(value: String): ParsedTicket {
        val compact = value.replace(" ", "")
        val roundToken =
            Regex("""^(\d+)""")
                .find(compact)
                ?.groupValues
                ?.getOrNull(1)
                ?: error("회차 정보가 없습니다.")

        val round = roundToken.toInt()
        val payload = compact.removePrefix(roundToken)
        val gameTokens =
            Regex("""\d{12}""")
                .findAll(payload)
                .map { it.value }
                .toList()
                .take(5)

        require(gameTokens.isNotEmpty()) { "번호 payload 형식이 올바르지 않습니다." }

        val games =
            gameTokens.map { chunk ->
                chunk.chunked(2).map { numberToken -> LottoNumber(numberToken.toInt()) }
            }

        return ParsedTicket(round = round, games = games)
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery.split('&').associate { token ->
            val pair = token.split('=', limit = 2)
            val key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8)
            val value = URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            key to value
        }
    }
}
