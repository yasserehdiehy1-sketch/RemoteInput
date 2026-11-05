package com.remoteinput

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "remoteinput_settings")

object SettingsRepo {
    private val KEY_LAST_IP = stringPreferencesKey("last_ip")
    private val KEY_LAST_MODE = stringPreferencesKey("last_mode")

    suspend fun saveLastIp(ctx: Context, ip: String) {
        try {
            ctx.dataStore.edit { it[KEY_LAST_IP] = ip }
        } catch (_: Exception) {}
    }

    suspend fun getLastIp(ctx: Context): String? {
        return try {
            ctx.dataStore.data.map { it[KEY_LAST_IP] }.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveMode(ctx: Context, mode: String) {
        try {
            ctx.dataStore.edit { it[KEY_LAST_MODE] = mode }
        } catch (_: Exception) {}
    }

    suspend fun getMode(ctx: Context): String? {
        return try {
            ctx.dataStore.data.map { it[KEY_LAST_MODE] }.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }
}
