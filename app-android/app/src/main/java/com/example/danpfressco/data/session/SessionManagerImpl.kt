package com.example.danpfressco.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.danpfressco.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fressco_session")

class SessionManagerImpl(private val context: Context) : SessionManager {

    companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_SESSION_TOKEN = stringPreferencesKey("session_token")
    }

    override suspend fun saveSession(user: Usuario) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_NAME] = user.nombre
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_SESSION_TOKEN] = user.token
        }
    }

    override fun getSessionToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_SESSION_TOKEN]
        }
    }

    override fun getUserName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_USER_NAME]
        }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
