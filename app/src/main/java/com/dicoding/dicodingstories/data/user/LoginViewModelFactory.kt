package com.dicoding.dicodingstories.data.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstories.data.Injection
import com.dicoding.dicodingstories.ui.detail.DetailViewModel
import com.dicoding.dicodingstories.ui.login.LoginViewModel
import com.dicoding.dicodingstories.ui.main.MainViewModel
import com.dicoding.dicodingstories.ui.main.UserViewModel
import com.dicoding.dicodingstories.ui.maps.MapsViewModel
import com.dicoding.dicodingstories.ui.post.PostViewModel

class LoginViewModelFactory(private  val pref: LoginPreference) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T }
            modelClass.isAssignableFrom(UserViewModel::class.java)  -> {
                UserViewModel(pref) as T }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(pref) as T }
            modelClass.isAssignableFrom(PostViewModel::class.java) ->{
                PostViewModel(pref) as T }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(pref) as T }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

}