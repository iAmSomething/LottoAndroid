package com.weeklylotto.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    override suspend fun markRoundViewed(roundNumber: Int) {
        context.resultViewDataStore.edit { preferences ->
            preferences[LAST_VIEWED_ROUND_KEY] = roundNumber
        }
    }

    private companion object {
        val LAST_VIEWED_ROUND_KEY = intPreferencesKey("last_viewed_round")
    }
}
