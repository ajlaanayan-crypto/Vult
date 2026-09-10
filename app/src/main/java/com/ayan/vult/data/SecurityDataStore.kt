package com.ayan.vult.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

class SecurityDataStore(private val context: Context) {
    companion object {
        val UNLOCK_DURATION = longPreferencesKey("unlock_duration")
    }

    // Fixed security code as requested by the user
    val securityCode: Flow<String> = flowOf("251000885")

    val unlockDuration: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[UNLOCK_DURATION] ?: 60000L // Default 1 minute
        }

    suspend fun updateUnlockDuration(duration: Long) {
        context.dataStore.edit { preferences ->
            preferences[UNLOCK_DURATION] = duration
        }
    }

    suspend fun updateSecurityCode(code: String) {
        // No-op as the PIN is now fixed
    }
}
