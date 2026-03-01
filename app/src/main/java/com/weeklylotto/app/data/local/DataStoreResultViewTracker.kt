package com.weeklylotto.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weeklylotto.app.domain.service.ResultViewTracker
import kotlinx.coroutines.flow.first

private val Context.resultViewDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "result_view_tracker",
)

class DataStoreResultViewTracker(
    private val context: Context,
) : ResultViewTracker {
    override suspend fun loadLastViewedRound(): Int? {
        val preferences = context.resultViewDataStore.data.first()
        return preferences[LAST_VIEWED_ROUND_KEY]
    }

    override suspend fun loadRecentViewedRounds(limit: Int): List<Int> {
        val preferences = context.resultViewDataStore.data.first()
        val storedRounds =
            parseRoundHistory(preferences[RECENT_VIEWED_ROUNDS_KEY])
                .distinct()
        if (storedRounds.isNotEmpty()) {
            return storedRounds.take(limit.coerceAtLeast(1))
        }
        return listOfNotNull(preferences[LAST_VIEWED_ROUND_KEY]).take(limit.coerceAtLeast(1))
    }

    override suspend fun markRoundViewed(roundNumber: Int) {
        context.resultViewDataStore.edit { preferences ->
            preferences[LAST_VIEWED_ROUND_KEY] = roundNumber
            val merged =
                buildList {
                    add(roundNumber)
                    addAll(parseRoundHistory(preferences[RECENT_VIEWED_ROUNDS_KEY]))
                }.distinct()
            preferences[RECENT_VIEWED_ROUNDS_KEY] = merged.take(MAX_HISTORY_SIZE).joinToString(",")
        }
    }

    private fun parseRoundHistory(csv: String?): List<Int> =
        csv
            ?.split(',')
            ?.mapNotNull { token -> token.trim().toIntOrNull() }
            ?.filter { it > 0 }
            ?: emptyList()

    private companion object {
        val LAST_VIEWED_ROUND_KEY = intPreferencesKey("last_viewed_round")
        val RECENT_VIEWED_ROUNDS_KEY = stringPreferencesKey("recent_viewed_rounds")
        const val MAX_HISTORY_SIZE = 16
    }
}
