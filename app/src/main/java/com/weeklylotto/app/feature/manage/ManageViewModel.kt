package com.weeklylotto.app.feature.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.data.RoundEstimator
import com.weeklylotto.app.domain.model.TicketBundle
import com.weeklylotto.app.domain.model.TicketSource
import com.weeklylotto.app.domain.model.TicketStatus
import com.weeklylotto.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ManageTab {
    WEEK,
    VAULT,
    SCAN,
}

data class ManageFilter(
    val statuses: Set<TicketStatus> = emptySet(),
    val roundRange: IntRange? = null,
)

data class ManageUiState(
    val tab: ManageTab = ManageTab.WEEK,
    val editMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val filter: ManageFilter = ManageFilter(),
    val isFabSheetOpen: Boolean = false,
    val isFilterSheetOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val tickets: List<TicketBundle> = emptyList(),
)

@Suppress("TooManyFunctions")
class ManageViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageUiState())
    val uiState: StateFlow<ManageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ticketRepository.observeAllTickets().collect { bundles ->
                _uiState.update { it.copy(tickets = bundles) }
            }
        }
    }

    fun setTab(tab: ManageTab) {
        _uiState.update {
            it.copy(
                tab = tab,
                selectedIds = emptySet(),
                editMode = false,
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update {
            it.copy(
                editMode = !it.editMode,
                selectedIds = if (it.editMode) emptySet() else it.selectedIds,
            )
        }
    }

    fun toggleSelection(bundleId: Long) {
        _uiState.update { state ->
            val updated =
                state.selectedIds.toMutableSet().apply {
                    if (contains(bundleId)) remove(bundleId) else add(bundleId)
                }
            state.copy(selectedIds = updated)
        }
    }

    fun openFabSheet() {
        _uiState.update { it.copy(isFabSheetOpen = true) }
    }

    fun closeFabSheet() {
        _uiState.update { it.copy(isFabSheetOpen = false) }
    }

    fun openFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = true) }
    }

    fun closeFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
    }

    fun toggleStatusFilter(status: TicketStatus) {
        _uiState.update { state ->
            val updated =
                state.filter.statuses.toMutableSet().apply {
                    if (contains(status)) remove(status) else add(status)
                }
            state.copy(filter = state.filter.copy(statuses = updated))
        }
    }

    fun clearFilter() {
        _uiState.update { it.copy(filter = ManageFilter()) }
    }

    fun setRoundRange(range: IntRange?) {
        _uiState.update { state ->
            state.copy(filter = state.filter.copy(roundRange = range))
        }
    }

    fun requestDeleteSelected() {
        _uiState.update { it.copy(isDeleteDialogOpen = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogOpen = false) }
    }

    fun deleteSelected() {
        val selectedIds = uiState.value.selectedIds
        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(isDeleteDialogOpen = false) }
            return
        }

        viewModelScope.launch {
            ticketRepository.deleteByIds(selectedIds)
            _uiState.update {
                it.copy(
                    selectedIds = emptySet(),
                    editMode = false,
                    isDeleteDialogOpen = false,
                )
            }
        }
    }

    fun filteredTickets(): List<TicketBundle> {
        val state = uiState.value
        val currentRound = RoundEstimator.currentSalesRound(LocalDate.now())
        val tabFiltered =
            when (state.tab) {
                ManageTab.WEEK -> state.tickets.filter { it.round.number == currentRound }
                ManageTab.VAULT -> state.tickets.filter { it.round.number != currentRound }
                ManageTab.SCAN -> state.tickets.filter { it.source == TicketSource.QR_SCAN }
            }
        val statusFiltered =
            if (state.filter.statuses.isEmpty()) {
                tabFiltered
            } else {
                tabFiltered.filter { it.status in state.filter.statuses }
            }

        val range = state.filter.roundRange
        return if (range == null) {
            statusFiltered
        } else {
            statusFiltered.filter { it.round.number in range }
        }
    }
}
