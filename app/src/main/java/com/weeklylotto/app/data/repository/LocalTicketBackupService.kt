package com.weeklylotto.app.data.repository

import com.weeklylotto.app.domain.error.AppResult
import com.weeklylotto.app.domain.model.DrawResult
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.PrizeAmountPolicy
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.AnalyticsEvent
import com.weeklylotto.app.domain.service.AnalyticsLogger
import com.weeklylotto.app.domain.service.AnalyticsParamKey
import com.weeklylotto.app.domain.service.NoOpAnalyticsLogger
import com.weeklylotto.app.domain.service.ResultEvaluator
import com.weeklylotto.app.domain.service.TicketBackupIntegritySummary
import com.weeklylotto.app.domain.service.TicketBackupService
import com.weeklylotto.app.domain.service.TicketBackupSummary
import com.weeklylotto.app.domain.service.TicketHistoryCsvSummary
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate

@Suppress("LongParameterList")
class LocalTicketBackupService(
    private val ticketRepository: TicketRepository,
    private val backupFile: File,
    private val drawRepository: DrawRepository? = null,
    private val resultEvaluator: ResultEvaluator? = null,
    private val aiHistoryCsvFile: File = File(backupFile.parentFile ?: File("."), AI_HISTORY_CSV_FILE_NAME),
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val analyticsLogger: AnalyticsLogger = NoOpAnalyticsLogger,
) : TicketBackupService {
    override suspend fun backupCurrentTickets(): Result<TicketBackupSummary> =
        runCatching {
            val tickets = ticketRepository.observeAllTickets().first()
            val payload =
                buildJsonObject {
                    put("schemaVersion", JsonPrimitive(BACKUP_SCHEMA_VERSION))
                    put("exportedAt", JsonPrimitive(Instant.now().toString()))
                    put(
                        "tickets",
                        buildJsonArray {
                            tickets.forEach { ticket ->
                                add(ticket.toBackupJson())
                            }
                        },
                    )
                }
            backupFile.parentFile?.mkdirs()
            backupFile.writeText(json.encodeToString(JsonObject.serializer(), payload), StandardCharsets.UTF_8)
            TicketBackupSummary(
                ticketCount = tickets.size,
                gameCount = tickets.sumOf { it.games.size },
                fileName = backupFile.name,
            )
        }

    override suspend fun restoreLatestBackup(): Result<TicketBackupSummary> =
        runCatching {
            require(backupFile.exists()) { "백업 파일이 없습니다." }
            val root = json.parseToJsonElement(backupFile.readText(StandardCharsets.UTF_8)).jsonObject
            val version = root.requiredInt("schemaVersion")
            require(version == BACKUP_SCHEMA_VERSION) {
                "지원하지 않는 백업 버전입니다. version=$version"
            }

            val restoredTickets = root.requiredArray("tickets").map { it.jsonObject.toTicketBundle() }
            val existingIds =
                ticketRepository
                    .observeAllTickets()
                    .first()
                    .mapNotNull { ticket -> ticket.id.takeIf { id -> id > 0L } }
                    .toSet()
            if (existingIds.isNotEmpty()) {
                ticketRepository.deleteByIds(existingIds)
            }
            restoredTickets.forEach { ticketRepository.save(it) }

            TicketBackupSummary(
                ticketCount = restoredTickets.size,
                gameCount = restoredTickets.sumOf { it.games.size },
                fileName = backupFile.name,
            )
        }

    override suspend fun verifyLatestBackupIntegrity(): Result<TicketBackupIntegritySummary> =
        runCatching {
            require(backupFile.exists()) { "백업 파일이 없습니다." }
            val root = json.parseToJsonElement(backupFile.readText(StandardCharsets.UTF_8)).jsonObject
            val version = root.requiredInt("schemaVersion")
            require(version == BACKUP_SCHEMA_VERSION) {
                "지원하지 않는 백업 버전입니다. version=$version"
            }

            val tickets = root.requiredArray("tickets")
            var gameCount = 0
            var invalidGameCount = 0
            var brokenTicketCount = 0
            val signatureCount = mutableMapOf<String, Int>()

            tickets.forEach { ticketElement ->
                val ticketObject = ticketElement as? JsonObject
                if (ticketObject == null) {
                    brokenTicketCount += 1
                    return@forEach
                }

                val parsed = ticketObject.toIntegrityTicketOrNull()
                if (parsed == null) {
                    brokenTicketCount += 1
                    return@forEach
                }

                gameCount += parsed.gameSignatures.size
                invalidGameCount += parsed.invalidGameCount
                signatureCount[parsed.signature] = signatureCount.getOrDefault(parsed.signature, 0) + 1
            }

            val duplicateTicketCount =
                signatureCount
                    .values
                    .sumOf { count -> (count - 1).coerceAtLeast(0) }
            val issueCount = duplicateTicketCount + invalidGameCount + brokenTicketCount
            val status = if (issueCount == 0) "pass" else "warn"

            analyticsLogger.log(
                event = AnalyticsEvent.OPS_DATA_INTEGRITY,
                params =
                    mapOf(
                        AnalyticsParamKey.SCREEN to "settings",
                        AnalyticsParamKey.COMPONENT to "backup_integrity",
                        AnalyticsParamKey.ACTION to "verify",
                        AnalyticsParamKey.STATUS to status,
                        AnalyticsParamKey.ISSUE_COUNT to issueCount.toString(),
                    ),
            )

            TicketBackupIntegritySummary(
                ticketCount = tickets.size,
                gameCount = gameCount,
                duplicateTicketCount = duplicateTicketCount,
                invalidGameCount = invalidGameCount,
                brokenTicketCount = brokenTicketCount,
                issueCount = issueCount,
                fileName = backupFile.name,
            )
        }

    override suspend fun exportTicketHistoryCsvForAi(): Result<TicketHistoryCsvSummary> =
        runCatching {
            val tickets =
                ticketRepository
                    .observeAllTickets()
                    .first()
                    .sortedWith(compareBy<TicketBundle> { it.round.number }.thenBy { it.createdAt })
            val rounds =
                tickets
                    .map { it.round }
                    .distinctBy { it.number }
            val sortedRoundNumbers = rounds.map { it.number }.sorted()
            val drawResultsByRound =
                rounds.associate { round ->
                    round.number to fetchDrawResultOrNull(round)
                }
            val csvBuildResult = buildTicketHistoryCsvRows(tickets, drawResultsByRound)
            aiHistoryCsvFile.parentFile?.mkdirs()
            aiHistoryCsvFile.writeText(
                buildCsvContent(csvBuildResult.rows),
                StandardCharsets.UTF_8,
            )
            val matchedDrawCount = drawResultsByRound.count { (_, drawResult) -> drawResult != null }
            val missingRoundNumbers =
                drawResultsByRound
                    .filterValues { drawResult -> drawResult == null }
                    .keys
                    .sorted()
            TicketHistoryCsvSummary(
                ticketCount = tickets.size,
                gameCount = tickets.sumOf { it.games.size },
                roundCount = rounds.size,
                firstRoundNumber = sortedRoundNumbers.firstOrNull(),
                lastRoundNumber = sortedRoundNumbers.lastOrNull(),
                matchedDrawCount = matchedDrawCount,
                missingDrawCount = missingRoundNumbers.size,
                missingRoundNumbers = missingRoundNumbers,
                winningGameCount = csvBuildResult.winningGameCount,
                totalExpectedPrizeAmount = csvBuildResult.totalExpectedPrizeAmount,
                fileName = aiHistoryCsvFile.name,
                filePath = aiHistoryCsvFile.absolutePath,
            )
        }

    private fun TicketBundle.toBackupJson(): JsonObject =
        buildJsonObject {
            put("roundNumber", JsonPrimitive(round.number))
            put("drawDate", JsonPrimitive(round.drawDate.toString()))
            put("source", JsonPrimitive(source.name))
            put("status", JsonPrimitive(status.name))
            put("createdAt", JsonPrimitive(createdAt.toString()))
            put(
                "games",
                buildJsonArray {
                    games.forEach { game ->
                        add(
                            buildJsonObject {
                                put("slot", JsonPrimitive(game.slot.name))
                                put("mode", JsonPrimitive(game.mode.name))
                                put(
                                    "numbers",
                                    buildJsonArray {
                                        game.numbers.forEach { number -> add(JsonPrimitive(number.value)) }
                                    },
                                )
                                put(
                                    "lockedNumbers",
                                    buildJsonArray {
                                        game.lockedNumbers.forEach { number -> add(JsonPrimitive(number.value)) }
                                    },
                                )
                            },
                        )
                    }
                },
            )
        }

    private fun JsonObject.toTicketBundle(): TicketBundle {
        val games =
            requiredArray("games").mapIndexed { index, gameElement ->
                val gameObject = gameElement.jsonObject
                val slotName = gameObject.optionalString("slot") ?: GameSlot.entries[index].name
                val numbers =
                    gameObject.requiredArray("numbers").map {
                            element ->
                        LottoNumber(element.requiredIntValue())
                    }
                val lockedNumbers =
                    gameObject
                        .requiredArray("lockedNumbers")
                        .map { element -> LottoNumber(element.requiredIntValue()) }
                        .toSet()
                LottoGame(
                    slot = GameSlot.valueOf(slotName),
                    numbers = numbers,
                    lockedNumbers = lockedNumbers,
                    mode = GameMode.valueOf(gameObject.requiredString("mode")),
                )
            }
        return TicketBundle(
            round =
                Round(
                    number = requiredInt("roundNumber"),
                    drawDate = LocalDate.parse(requiredString("drawDate")),
                ),
            source = TicketSource.valueOf(requiredString("source")),
            status = TicketStatus.valueOf(requiredString("status")),
            createdAt = Instant.parse(requiredString("createdAt")),
            games = games,
        )
    }

    private suspend fun fetchDrawResultOrNull(round: Round): DrawResult? =
        when (val drawResult = drawRepository?.fetchByRound(round)) {
            is AppResult.Success -> drawResult.value
            else -> null
        }

    private fun buildTicketHistoryCsvRows(
        tickets: List<TicketBundle>,
        drawResultsByRound: Map<Int, DrawResult?>,
    ): TicketHistoryCsvBuildResult {
        var winningGameCount = 0
        var totalExpectedPrizeAmount = 0L
        val rows =
            tickets.flatMap { ticket ->
                val drawResult = drawResultsByRound[ticket.round.number]
                ticket.games.map { game ->
                    val evaluation = drawResult?.let { draw -> resultEvaluator?.evaluate(game, draw) }
                    val expectedPrizeAmount = evaluation?.let { PrizeAmountPolicy.amountFor(it.rank) }
                    if ((expectedPrizeAmount ?: 0L) > 0L) {
                        winningGameCount += 1
                        totalExpectedPrizeAmount += expectedPrizeAmount ?: 0L
                    }
                    listOf(
                        ticket.round.number.toString(),
                        ticket.round.drawDate.toString(),
                        ticket.id.toString(),
                        ticket.source.name,
                        ticket.status.name,
                        ticket.createdAt.toString(),
                        game.slot.name,
                        game.mode.name,
                        game.numbers.map { it.value }.sorted().joinToString(separator = "-"),
                        drawResult?.mainNumbers?.map { it.value }?.sorted()?.joinToString(separator = "-")
                            .orEmpty(),
                        drawResult?.bonus?.value?.toString().orEmpty(),
                        if (drawResult == null) "N" else "Y",
                        evaluation?.matchedMainCount?.toString().orEmpty(),
                        evaluation?.bonusMatched?.toString().orEmpty(),
                        evaluation?.rank?.name.orEmpty(),
                        expectedPrizeAmount?.toString().orEmpty(),
                    )
                }
            }
        return TicketHistoryCsvBuildResult(
            rows = rows,
            winningGameCount = winningGameCount,
            totalExpectedPrizeAmount = totalExpectedPrizeAmount,
        )
    }

    private fun buildCsvContent(rows: List<List<String>>): String {
        val builder = StringBuilder()
        builder.appendLine(TICKET_HISTORY_CSV_HEADERS.joinToString(separator = ","))
        rows.forEach { row ->
            builder.appendLine(row.joinToString(separator = ",") { value -> value.toCsvCell() })
        }
        return builder.toString()
    }
}

private const val BACKUP_SCHEMA_VERSION = 1
private const val AI_HISTORY_CSV_FILE_NAME = "tickets_history_with_draw_latest.csv"
private val TICKET_HISTORY_CSV_HEADERS =
    listOf(
        "round_number",
        "round_draw_date",
        "ticket_id",
        "ticket_source",
        "ticket_status",
        "ticket_created_at",
        "game_slot",
        "game_mode",
        "game_numbers",
        "draw_main_numbers",
        "draw_bonus_number",
        "draw_matched",
        "matched_main_count",
        "bonus_matched",
        "draw_rank",
        "expected_prize_amount",
    )

private fun JsonObject.requiredString(key: String): String =
    this[key]?.jsonPrimitive?.contentOrNull ?: error("백업 데이터에 `$key` 값이 없습니다.")

private fun JsonObject.optionalString(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.requiredInt(key: String): Int =
    this[key]?.jsonPrimitive?.intOrNull ?: error("백업 데이터에 `$key` 값이 없습니다.")

private fun JsonObject.requiredArray(key: String): JsonArray = this[key]?.jsonArray ?: error("백업 데이터에 `$key` 배열이 없습니다.")

private fun JsonElement.requiredIntValue(): Int = this.jsonPrimitive.intOrNull ?: error("백업 데이터 숫자 값이 올바르지 않습니다.")

private fun String.toCsvCell(): String {
    val escaped = replace("\"", "\"\"")
    return if (contains(',') || contains('"') || contains('\n')) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private data class TicketHistoryCsvBuildResult(
    val rows: List<List<String>>,
    val winningGameCount: Int,
    val totalExpectedPrizeAmount: Long,
)

private data class IntegrityTicket(
    val signature: String,
    val gameSignatures: List<String>,
    val invalidGameCount: Int,
)

private fun JsonObject.toIntegrityTicketOrNull(): IntegrityTicket? {
    val roundNumber = this["roundNumber"]?.jsonPrimitive?.intOrNull
    val drawDate = this["drawDate"]?.jsonPrimitive?.contentOrNull
    val source = this["source"]?.jsonPrimitive?.contentOrNull
    val status = this["status"]?.jsonPrimitive?.contentOrNull
    val createdAt = this["createdAt"]?.jsonPrimitive?.contentOrNull
    val games = this["games"]?.jsonArray

    val requiredValues = listOf(roundNumber, drawDate, source, status, createdAt, games)
    if (requiredValues.any { value -> value == null }) {
        return null
    }
    val safeRoundNumber = roundNumber!!
    val safeDrawDate = drawDate!!
    val safeSource = source!!
    val safeStatus = status!!
    val safeCreatedAt = createdAt!!
    val safeGames = games!!

    var invalidGameCount = 0
    val gameSignatures = mutableListOf<String>()
    safeGames.forEach { gameElement ->
        val gameObject = gameElement as? JsonObject
        val numbers =
            gameObject
                ?.get("numbers")
                ?.jsonArray
                ?.mapNotNull { value -> value.jsonPrimitive.intOrNull }
                ?: emptyList()
        val isValid =
            numbers.size == 6 &&
                numbers.toSet().size == 6 &&
                numbers.all { number -> number in 1..45 }
        if (!isValid) {
            invalidGameCount += 1
            return@forEach
        }
        val sortedSignature = numbers.sorted().joinToString(separator = "-")
        gameSignatures += sortedSignature
    }

    val ticketSignature =
        buildString {
            append(safeRoundNumber)
            append('|')
            append(safeDrawDate)
            append('|')
            append(safeSource)
            append('|')
            append(safeStatus)
            append('|')
            append(safeCreatedAt)
            append('|')
            append(gameSignatures.sorted().joinToString(separator = ","))
        }
    return IntegrityTicket(
        signature = ticketSignature,
        gameSignatures = gameSignatures,
        invalidGameCount = invalidGameCount,
    )
}
