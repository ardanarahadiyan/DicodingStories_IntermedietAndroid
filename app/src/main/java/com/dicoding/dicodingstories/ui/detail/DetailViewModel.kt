package com.dicoding.dicodingstories.ui.detail

import android.util.Log
import androidx.lifecycle.*
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.SinglePostResponse
import com.dicoding.dicodingstories.data.remote.Story
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.ui.main.MainViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel (private val pref: LoginPreference): ViewModel(){

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError

    private val _singlePost = MutableLiveData<Story?>()
    val singlePost : MutableLiveData<Story?> = _singlePost

    fun isLogin() : LiveData<LoginModel> {
        return pref.isLogin().asLiveData()
    }

    fun logout(){
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun getSinglePost(header: String, storyId : String){
        _isLoading.value = true
        val client = ApiConfig.getApiService().getSinglePost("Bearer $header", storyId)
        client.enqueue(object : Callback<SinglePostResponse>{
            override fun onResponse(
                call: Call<SinglePostResponse>,
                response: Response<SinglePostResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    _singlePost.value = response.body()?.story
                }else{
                    Log.e(MainViewModel.TAG, "onFailure: ${response.message()}")
                    _snackbarError.value = Event("onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SinglePostResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(MainViewModel.TAG, "onFailure: ${t.message}")
                _snackbarError.value = Event("onFailure: ${t.message}")
            }

        })

    }

}