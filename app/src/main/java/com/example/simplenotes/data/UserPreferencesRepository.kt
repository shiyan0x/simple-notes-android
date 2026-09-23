package com.example.simplenotes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_NOTE_COLOR = stringPreferencesKey("default_note_color")
    }

    // Values: "system", "light", "dark"
    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    // Values: "DEFAULT", "RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "PURPLE"
    val defaultNoteColor: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_NOTE_COLOR] ?: "DEFAULT"
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setDefaultNoteColor(colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_NOTE_COLOR] = colorHex
        }
    }
}
