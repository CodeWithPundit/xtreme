package com.xtremeiptv.core.common.manager

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.xtremeiptv.core.common.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Settings DataStore keys
    private object PreferencesKeys {
        val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val DEFAULT_PLAYER = stringPreferencesKey("default_player")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val BACKGROUND_PLAY = booleanPreferencesKey("background_play")
        val SUBTITLE_SIZE = intPreferencesKey("subtitle_size")
        val SUBTITLE_COLOR = stringPreferencesKey("subtitle_color")
    }

    // Disclaimer
    suspend fun isDisclaimerAccepted(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[PreferencesKeys.DISCLAIMER_ACCEPTED] ?: false
        }.first()
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        applicationScope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.DISCLAIMER_ACCEPTED] = accepted
            }
        }
    }

    // First Launch
    suspend fun isFirstLaunch(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[PreferencesKeys.FIRST_LAUNCH] ?: true
        }.first()
    }

    fun setFirstLaunchComplete() {
        applicationScope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.FIRST_LAUNCH] = false
            }
        }
    }

    // Last Sync Time
    fun getLastSyncTime(): Flow<Long> {
        return context.dataStore.data.map { prefs ->
            prefs[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
        }
    }

    fun updateLastSyncTime() {
        applicationScope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.LAST_SYNC_TIME] = System.currentTimeMillis()
            }
        }
    }

    // Player Settings
    fun getDefaultPlayer(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[PreferencesKeys.DEFAULT_PLAYER] ?: "exo"
        }
    }

    fun setDefaultPlayer(player: String) {
        applicationScope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.DEFAULT_PLAYER] = player
            }
        }
    }

    fun isAutoPlayEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[PreferencesKeys.AUTO_PLAY] ?: true
        }
    }

    fun setAutoPlayEnabled(enabled: Boolean) {
        applicationScope.launch {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.AUTO_PLAY] = enabled
            }
        }
    }

    // Secure preferences (Encrypted)
    fun saveSecureString(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun getSecureString(key: String, defaultValue: String = ""): String {
        return encryptedPrefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveSecureBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit().putBoolean(key, value).apply()
    }

    fun getSecureBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }

    fun removeSecureKey(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    fun clearSecurePrefs() {
        encryptedPrefs.edit().clear().apply()
    }
}
