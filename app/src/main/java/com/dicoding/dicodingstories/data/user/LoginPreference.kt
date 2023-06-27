package com.dicoding.dicodingstories.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LoginPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun login(login: LoginModel) {
        dataStore.edit { preferences ->
            preferences[SESSION_KEY] = true
            preferences[TOKEN_KEY] = login.token
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[SESSION_KEY] = false
            preferences[TOKEN_KEY] = ""
        }
    }

    fun isLogin(): Flow<LoginModel> {
        return dataStore.data.map { preferences ->
            LoginModel(
                preferences[SESSION_KEY] ?: false,
                preferences[TOKEN_KEY] ?: ""
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LoginPreference? = null

        private val SESSION_KEY = booleanPreferencesKey("session")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<Preferences>): LoginPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = LoginPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}