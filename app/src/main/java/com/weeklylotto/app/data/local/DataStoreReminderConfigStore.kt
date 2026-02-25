package com.weeklylotto.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weeklylotto.app.domain.model.ReminderConfig
import com.weeklylotto.app.domain.service.ReminderConfigStore
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalTime

private val Context.reminderConfigDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reminder_config",
)

class DataStoreReminderConfigStore(
    private val context: Context,
) : ReminderConfigStore {
    override suspend fun load(): ReminderConfig {
        val preferences = context.reminderConfigDataStore.data.first()

        return ReminderConfig(
            purchaseReminderDay =
                preferences[PURCHASE_DAY_KEY]
                    ?.let { DayOfWeek.valueOf(it) }
                    ?: DayOfWeek.SATURDAY,
            purchaseReminderTime =
                preferences[PURCHASE_TIME_KEY]
                    ?.let { LocalTime.parse(it) }
                    ?: LocalTime.of(15, 0),
            resultReminderDay =
                preferences[RESULT_DAY_KEY]
                    ?.let { DayOfWeek.valueOf(it) }
                    ?: DayOfWeek.SATURDAY,
            resultReminderTime =
                preferences[RESULT_TIME_KEY]
                    ?.let { LocalTime.parse(it) }
                    ?: LocalTime.of(21, 0),
            enabled = preferences[ENABLED_KEY] ?: true,
        )
    }

    override suspend fun save(config: ReminderConfig) {
        context.reminderConfigDataStore.edit { preferences ->
            preferences[PURCHASE_DAY_KEY] = config.purchaseReminderDay.name
            preferences[PURCHASE_TIME_KEY] = config.purchaseReminderTime.toString()
            preferences[RESULT_DAY_KEY] = config.resultReminderDay.name
            preferences[RESULT_TIME_KEY] = config.resultReminderTime.toString()
            preferences[ENABLED_KEY] = config.enabled
        }
    }

    private companion object {
        val PURCHASE_DAY_KEY = stringPreferencesKey("purchase_day")
        val PURCHASE_TIME_KEY = stringPreferencesKey("purchase_time")
        val RESULT_DAY_KEY = stringPreferencesKey("result_day")
        val RESULT_TIME_KEY = stringPreferencesKey("result_time")
        val ENABLED_KEY = booleanPreferencesKey("enabled")
    }
}
