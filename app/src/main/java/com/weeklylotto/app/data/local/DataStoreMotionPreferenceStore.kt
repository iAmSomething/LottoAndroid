package com.weeklylotto.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.weeklylotto.app.domain.service.MotionPreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.motionPreferenceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "motion_preference",
)

class DataStoreMotionPreferenceStore(
    private val context: Context,
) : MotionPreferenceStore {
    override fun observeReduceMotionEnabled(): Flow<Boolean> =
        context.motionPreferenceDataStore.data.map { preferences ->
            preferences[REDUCE_MOTION_ENABLED_KEY] ?: false
        }

    override suspend fun loadReduceMotionEnabled(): Boolean {
        val preferences = context.motionPreferenceDataStore.data.first()
        return preferences[REDUCE_MOTION_ENABLED_KEY] ?: false
    }

    override suspend fun saveReduceMotionEnabled(enabled: Boolean) {
        context.motionPreferenceDataStore.edit { preferences ->
            preferences[REDUCE_MOTION_ENABLED_KEY] = enabled
        }
    }

    private companion object {
        val REDUCE_MOTION_ENABLED_KEY = booleanPreferencesKey("reduce_motion_enabled")
    }
}
