package com.weeklylotto.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.weeklylotto.app.domain.service.PurchaseRedirectNoticeStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStorePurchaseRedirectNoticeStore(
    private val context: Context,
) : PurchaseRedirectNoticeStore {
    override suspend fun hasSeenNotice(): Boolean =
        context.dataStore.data
            .map { preferences -> preferences[HAS_SEEN_KEY] ?: false }
            .first()

    override suspend fun markNoticeSeen() {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_KEY] = true
        }
    }

    private companion object {
        private val Context.dataStore by preferencesDataStore("purchase_redirect_notice")
        val HAS_SEEN_KEY = booleanPreferencesKey("purchase_redirect_has_seen")
    }
}
