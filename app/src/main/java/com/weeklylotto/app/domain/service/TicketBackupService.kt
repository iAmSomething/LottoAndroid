package com.weeklylotto.app.domain.service

data class TicketBackupSummary(
    val ticketCount: Int,
    val gameCount: Int,
    val fileName: String,
)

data class TicketBackupIntegritySummary(
    val ticketCount: Int,
    val gameCount: Int,
    val duplicateTicketCount: Int,
    val invalidGameCount: Int,
    val brokenTicketCount: Int,
    val issueCount: Int,
    val fileName: String,
)

interface TicketBackupService {
    suspend fun backupCurrentTickets(): Result<TicketBackupSummary>

    suspend fun restoreLatestBackup(): Result<TicketBackupSummary>

    suspend fun verifyLatestBackupIntegrity(): Result<TicketBackupIntegritySummary>
}

object NoOpTicketBackupService : TicketBackupService {
    private val error = IllegalStateException("백업 기능이 초기화되지 않았습니다.")

    override suspend fun backupCurrentTickets(): Result<TicketBackupSummary> = Result.failure(error)

    override suspend fun restoreLatestBackup(): Result<TicketBackupSummary> = Result.failure(error)

    override suspend fun verifyLatestBackupIntegrity(): Result<TicketBackupIntegritySummary> = Result.failure(error)
}
