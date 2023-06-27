package com.dicoding.dicodingstories.data.paging

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstories.data.Injection
import com.dicoding.dicodingstories.ui.main.MainViewModel

class PostViewModelFactory (private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create (modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return  MainViewModel(Injection.provideRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}