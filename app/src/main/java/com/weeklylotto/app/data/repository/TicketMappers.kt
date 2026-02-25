package com.weeklylotto.app.data.repository

import com.weeklylotto.app.data.local.TicketBundleEntity
import com.weeklylotto.app.data.local.TicketBundleWithGames
import com.weeklylotto.app.data.local.TicketGameEntity
import com.weeklylotto.app.domain.model.GameMode
import com.weeklylotto.app.domain.model.GameSlot
import com.weeklylotto.app.domain.model.LottoGame
import com.weeklylotto.app.domain.model.LottoNumber
import com.weeklylotto.app.domain.model.Round
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import java.time.Instant
import java.time.LocalDate

internal fun TicketBundle.toEntity(): TicketBundleEntity =
    TicketBundleEntity(
        id = id,
        roundNumber = round.number,
        drawDate = round.drawDate.toString(),
        source = source.name,
        status = status.name,
        createdAtEpochMillis = createdAt.toEpochMilli(),
    )

internal fun LottoGame.toEntity(bundleId: Long): TicketGameEntity =
    TicketGameEntity(
        bundleId = bundleId,
        slot = slot.name,
        numbers = numbers.map { it.value },
        lockedNumbers = lockedNumbers.map { it.value },
        mode = mode.name,
    )

internal fun TicketBundleWithGames.toDomain(): TicketBundle =
    TicketBundle(
        id = bundle.id,
        round =
            Round(
                number = bundle.roundNumber,
                drawDate = LocalDate.parse(bundle.drawDate),
            ),
        games =
            games.sortedBy { it.slot }.map { game ->
                LottoGame(
                    slot = GameSlot.valueOf(game.slot),
                    numbers = game.numbers.map(::LottoNumber).sortedBy { it.value },
                    lockedNumbers = game.lockedNumbers.map(::LottoNumber).toSet(),
                    mode = GameMode.valueOf(game.mode),
                )
            },
        source = TicketSource.valueOf(bundle.source),
        createdAt = Instant.ofEpochMilli(bundle.createdAtEpochMillis),
        status = TicketStatus.valueOf(bundle.status),
    )
