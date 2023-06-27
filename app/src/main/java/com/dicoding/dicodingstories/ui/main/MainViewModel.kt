package com.dicoding.dicodingstories.ui.main

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.paging.PostRepository
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.remote.PostResponse
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel (postRepository: PostRepository): ViewModel(){

    companion object{
        const val TAG = "MainViewModel"
    }

    val listLivePost : LiveData<PagingData<ListStoryItem>> = postRepository.getPost().cachedIn(viewModelScope)

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError



}