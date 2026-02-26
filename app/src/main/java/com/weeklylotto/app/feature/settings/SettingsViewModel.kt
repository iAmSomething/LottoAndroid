package com.weeklylotto.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.MotionPreferenceStore
import com.weeklylotto.app.domain.service.ReminderConfigStore
import com.weeklylotto.app.domain.service.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val config: ReminderConfig = ReminderConfig(),
    val reduceMotionEnabled: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(
    private val reminderConfigStore: ReminderConfigStore,
    private val reminderScheduler: ReminderScheduler,
    private val motionPreferenceStore: MotionPreferenceStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedConfig = reminderConfigStore.load()
            val reduceMotionEnabled = motionPreferenceStore.loadReduceMotionEnabled()
            _uiState.update {
                it.copy(
                    config = savedConfig,
                    reduceMotionEnabled = reduceMotionEnabled,
                )
            }
        }
    }

    fun useDefaultSchedule() {
        _uiState.update { it.copy(config = ReminderConfig()) }
    }

    fun useFridayEveningSchedule() {
        _uiState.update {
            it.copy(
                config =
                    it.config.copy(
                        purchaseReminderDay = DayOfWeek.FRIDAY,
                        purchaseReminderTime = LocalTime.of(18, 30),
                    ),
            )
        }
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(config = it.config.copy(enabled = enabled))
        }
    }

    fun setReduceMotionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(reduceMotionEnabled = enabled) }
        viewModelScope.launch {
            motionPreferenceStore.saveReduceMotionEnabled(enabled)
        }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            val config = _uiState.value.config
            reminderConfigStore.save(config)
            reminderScheduler.schedule(config)
            _uiState.update { it.copy(message = "알림 설정을 저장했습니다.") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
