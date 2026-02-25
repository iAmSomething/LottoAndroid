package com.weeklylotto.app.data.network

import com.weeklylotto.app.domain.error.AppError
import com.weeklylotto.app.domain.error.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DrawApiClient(
    private val baseUrl: String,
    private val mirrorBaseUrl: String = "https://smok95.github.io/lotto/results",
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchRound(round: Int): AppResult<DrawApiPayload> =
        withContext(Dispatchers.IO) {
            val officialUrl = "$baseUrl/common.do?method=getLottoNumber&drwNo=$round"
            val officialResult =
                fetchJson(officialUrl).let { fetched ->
                    when (fetched) {
                        is AppResult.Success -> parseOfficialPayload(fetched.value, round)
                        is AppResult.Failure -> fetched
                    }
                }

            if (officialResult is AppResult.Success) {
                return@withContext officialResult
            }

            val mirrorUrl = "$mirrorBaseUrl/$round.json"
            val mirrorResult =
                fetchJson(mirrorUrl).let { fetched ->
                    when (fetched) {
                        is AppResult.Success -> parseMirrorPayload(fetched.value, round)
                        is AppResult.Failure -> fetched
                    }
                }

            if (mirrorResult is AppResult.Success) {
                return@withContext mirrorResult
            }

            officialResult
        }

    private fun fetchJson(url: String): AppResult<String> =
        runCatching {
            val connection =
                (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/json, text/plain, */*")
                    setRequestProperty("User-Agent", "WeeklyLottoApp/1.0")
                }

            val status = connection.responseCode
            val stream =
                if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: connection.inputStream
                }

            val body = stream.bufferedReader().use { it.readText() }
            connection.disconnect()

            if (status !in 200..299) {
                throw IOException("HTTP $status: ${body.take(120)}")
            }
            body
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                when (throwable) {
                    is IOException ->
                        AppResult.Failure(
                            AppError.NetworkError("당첨 API 통신에 실패했습니다: ${throwable.message}"),
                        )

                    else ->
                        AppResult.Failure(
                            AppError.ParseError("당첨 API 응답 파싱에 실패했습니다: ${throwable.message}"),
                        )
                }
            },
        )

    internal fun parseOfficialPayload(
        body: String,
        requestedRound: Int,
    ): AppResult<DrawApiPayload> =
        runCatching {
            val objectNode = json.parseToJsonElement(body).jsonObject
            val returnValue = objectNode.getString("returnValue")
            if (returnValue != "success") {
                return AppResult.Failure(
                    AppError.NetworkError("회차($requestedRound) 당첨 정보를 찾지 못했습니다.", code = 404),
                )
            }

            AppResult.Success(
                DrawApiPayload(
                    round = objectNode.getInt("drwNo"),
                    drawDate = objectNode.getString("drwNoDate"),
                    main =
                        listOf(
                            objectNode.getInt("drwtNo1"),
                            objectNode.getInt("drwtNo2"),
                            objectNode.getInt("drwtNo3"),
                            objectNode.getInt("drwtNo4"),
                            objectNode.getInt("drwtNo5"),
                            objectNode.getInt("drwtNo6"),
                        ),
                    bonus = objectNode.getInt("bnusNo"),
                ),
            )
        }.getOrElse { throwable ->
            AppResult.Failure(
                AppError.ParseError("당첨 API 응답 파싱에 실패했습니다: ${throwable.message}"),
            )
        }

    internal fun parseMirrorPayload(
        body: String,
        requestedRound: Int,
    ): AppResult<DrawApiPayload> =
        runCatching {
            val objectNode = json.parseToJsonElement(body).jsonObject
            val round = objectNode.getInt("draw_no")
            if (round != requestedRound) {
                return AppResult.Failure(
                    AppError.ParseError("요청 회차($requestedRound)와 응답 회차($round)가 일치하지 않습니다."),
                )
            }
            val numbers = objectNode.getIntList("numbers")
            require(numbers.size == 6) { "numbers must contain 6 values." }
            val bonus = objectNode.getInt("bonus_no")
            val isoDate = objectNode.getString("date")
            val drawDate =
                OffsetDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate().toString()

            AppResult.Success(
                DrawApiPayload(
                    round = round,
                    drawDate = drawDate,
                    main = numbers,
                    bonus = bonus,
                ),
            )
        }.getOrElse { throwable ->
            AppResult.Failure(
                AppError.ParseError("미러 당첨 데이터 파싱에 실패했습니다: ${throwable.message}"),
            )
        }
}

data class DrawApiPayload(
    val round: Int,
    val drawDate: String,
    val main: List<Int>,
    val bonus: Int,
)

private fun Map<String, JsonElement>.getInt(key: String): Int =
    this[key]?.jsonPrimitive?.intOrNull ?: error("$key is missing or invalid")

private fun Map<String, JsonElement>.getString(key: String): String =
    this[key]?.jsonPrimitive?.content ?: error("$key is missing")

private fun Map<String, JsonElement>.getIntList(key: String): List<Int> =
    this[key]?.jsonArray?.mapIndexed { index, element ->
        element.jsonPrimitive.intOrNull ?: error("$key[$index] is invalid")
    } ?: error("$key is missing")
