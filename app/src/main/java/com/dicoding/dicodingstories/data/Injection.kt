package com.dicoding.dicodingstories.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.dicodingstories.data.paging.PostDatabase
import com.dicoding.dicodingstories.data.paging.PostRepository
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.user.LoginPreference

private val Context.ds : DataStore<Preferences> by preferencesDataStore("token")

object Injection {
    fun provideRepository(context : Context) : PostRepository{
        val preferences = LoginPreference.getInstance(context.ds)
        val apiService = ApiConfig.getApiService()
        return PostRepository(apiService,preferences)
    }
}