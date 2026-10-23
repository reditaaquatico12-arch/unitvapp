// language: Kotlin, file: PreferencesManager.kt, target: Android 21+
// DataStore — persiste server/user/pass e configurações do app
package com.unitvclone.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "unitv_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SERVER   = stringPreferencesKey("xtream_server")
        val KEY_USERNAME = stringPreferencesKey("xtream_username")
        val KEY_PASSWORD = stringPreferencesKey("xtream_password")
        val KEY_ACTIVATED = booleanPreferencesKey("is_activated")
        val KEY_PLAYER_BUFFER = intPreferencesKey("player_buffer_sec")  // default 10
        val KEY_USE_HW_DECODE = booleanPreferencesKey("hw_decode")
    }

    val server:   Flow<String>  = context.dataStore.data.map { it[KEY_SERVER]   ?: "" }
    val username: Flow<String>  = context.dataStore.data.map { it[KEY_USERNAME] ?: "" }
    val password: Flow<String>  = context.dataStore.data.map { it[KEY_PASSWORD] ?: "" }
    val isActivated: Flow<Boolean> = context.dataStore.data.map { it[KEY_ACTIVATED] ?: false }
    val bufferSec: Flow<Int>    = context.dataStore.data.map { it[KEY_PLAYER_BUFFER] ?: 10 }
    val hwDecode: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_HW_DECODE] ?: true }

    suspend fun saveCredentials(server: String, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER]   = server
            prefs[KEY_USERNAME] = username
            prefs[KEY_PASSWORD] = password
            prefs[KEY_ACTIVATED] = true
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER]    = ""
            prefs[KEY_USERNAME]  = ""
            prefs[KEY_PASSWORD]  = ""
            prefs[KEY_ACTIVATED] = false
        }
    }

    suspend fun setBufferSec(sec: Int) {
        context.dataStore.edit { it[KEY_PLAYER_BUFFER] = sec }
    }

    suspend fun setHwDecode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_USE_HW_DECODE] = enabled }
    }
}
