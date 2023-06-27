package com.dicoding.dicodingstories.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.launch

class UserViewModel (private val pref: LoginPreference): ViewModel() {

    fun isLogin() : LiveData<LoginModel> {
        return pref.isLogin().asLiveData()
    }

    fun logout(){
        viewModelScope.launch {
            pref.logout()
        }
    }
}