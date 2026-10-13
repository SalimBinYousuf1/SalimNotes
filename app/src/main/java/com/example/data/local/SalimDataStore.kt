package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_settings")

class SalimDataStore(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME = stringPreferencesKey("app_theme") // "system", "light", "dark"
        val KEY_LAYOUT_STYLE = stringPreferencesKey("layout_style") // "grid", "list"
        val KEY_AUTO_MOVE_CHECKED = booleanPreferencesKey("auto_move_checked")
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_AUTO_LOCK_TIMEOUT_MINS = intPreferencesKey("auto_lock_timeout_mins")
        val KEY_TRASH_RETENTION_DAYS = intPreferencesKey("trash_retention_days")
        val KEY_DEFAULT_SNOOZE_MINS = intPreferencesKey("default_snooze_mins")
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: ""
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "system"
    }

    val layoutStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAYOUT_STYLE] ?: "grid"
    }

    val autoMoveChecked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_MOVE_CHECKED] ?: true
    }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_LOCK_ENABLED] ?: false
    }

    val trashRetentionDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_TRASH_RETENTION_DAYS] ?: 30
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setLayoutStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAYOUT_STYLE] = style
        }
    }

    suspend fun setAutoMoveChecked(autoMove: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_MOVE_CHECKED] = autoMove
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setTrashRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TRASH_RETENTION_DAYS] = days
        }
    }
}
