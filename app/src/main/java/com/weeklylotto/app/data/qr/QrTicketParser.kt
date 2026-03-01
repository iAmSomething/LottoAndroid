package com.weeklylotto.app.data.qr

import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.ParsedTicket
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class QrTicketParser {
    fun parse(rawUrl: String): AppResult<ParsedTicket> {
        return runCatching {
            val uri = URI(rawUrl)
            val queryMap = parseQuery(uri.rawQuery)
            val queryRoundToken = queryMap["drwNo"]
            val queryRound = queryRoundToken?.toIntOrNull()
            val queryNumbers = queryMap["numbers"]
            val compact = queryMap["v"]

            if (queryRoundToken != null || queryNumbers != null) {
                if (queryRound == null) {
                    fail(
                        type = QrParseFailureType.INVALID_ROUND,
                        message = "회차 정보가 올바르지 않습니다.",
                    )
                }
                if (queryNumbers.isNullOrBlank()) {
                    fail(
                        type = QrParseFailureType.MISSING_PAYLOAD,
                        message = "번호 payload가 비어 있습니다.",
                    )
                }
                val games =
                    queryNumbers.split(';')
                        .filter { it.isNotBlank() }
                        .map { gameToken ->
                            gameToken
                                .split(',')
                                .map { token -> LottoNumber(token.trim().toInt()) }
                        }
                if (games.isEmpty()) {
                    fail(
                        type = QrParseFailureType.MISSING_PAYLOAD,
                        message = "QR에 게임 정보가 없습니다.",
                    )
                }
                return@runCatching ParsedTicket(queryRound, games)
            }

            if (!compact.isNullOrBlank()) {
                return@runCatching parseCompact(compact)
            }

            fail(
                type = QrParseFailureType.UNSUPPORTED_FORMAT,
                message = "지원하지 않는 QR URL 형식입니다.",
            )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                val type =
                    when (throwable) {
                        is QrParseException -> throwable.type
                        is NumberFormatException, is IllegalArgumentException -> QrParseFailureType.INVALID_NUMBER
                        else -> QrParseFailureType.UNKNOWN
                    }
                val detail = throwable.message ?: "QR 파싱 실패"
                AppResult.Failure(AppError.ParseError("[qr:${type.code}] $detail"))
            },
        )
    }

    private fun parseCompact(value: String): ParsedTicket {
        val compact = value.replace(" ", "")
        val roundToken =
            Regex("""^(\d+)""")
                .find(compact)
                ?.groupValues
                ?.getOrNull(1)
                ?: fail(
                    type = QrParseFailureType.INVALID_ROUND,
                    message = "회차 정보가 없습니다.",
                )

        val round = roundToken.toInt()
        val payload = compact.removePrefix(roundToken)
        val gameTokens =
            Regex("""\d{12}""")
                .findAll(payload)
                .map { it.value }
                .toList()
                .take(5)

        if (gameTokens.isEmpty()) {
            fail(
                type = QrParseFailureType.MISSING_PAYLOAD,
                message = "번호 payload 형식이 올바르지 않습니다.",
            )
        }

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

    private fun fail(
        type: QrParseFailureType,
        message: String,
    ): Nothing = throw QrParseException(type = type, message = message)
}

enum class QrParseFailureType(
    val code: String,
) {
    UNSUPPORTED_FORMAT("unsupported_format"),
    MISSING_PAYLOAD("missing_payload"),
    INVALID_ROUND("invalid_round"),
    INVALID_NUMBER("invalid_number"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromMessage(message: String): QrParseFailureType {
            val code =
                Regex("""\[qr:([a-z_]+)]""")
                    .find(message.lowercase(Locale.ROOT))
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: return UNKNOWN
            return entries.firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}

private class QrParseException(
    val type: QrParseFailureType,
    override val message: String,
) : IllegalArgumentException(message)
