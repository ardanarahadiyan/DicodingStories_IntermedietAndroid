package com.dicoding.dicodingstories.ui.post

import android.util.Log
import androidx.lifecycle.*
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.NewPostResponse
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostViewModel (private val pref: LoginPreference): ViewModel() {

    companion object{
        const val TAG = "PostViewModel"
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError

    private val _isError = MutableLiveData<Boolean>()
    val isError : LiveData<Boolean> = _isError

    fun isLogin() : LiveData<LoginModel> {
        return pref.isLogin().asLiveData()
    }

    fun logout(){
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun shareStories(header: String, imageMultipart: MultipartBody.Part, description: RequestBody, lat : RequestBody?, lon : RequestBody?){
        val client = ApiConfig.getApiService().uploadImage("Bearer $header", imageMultipart, description, lat, lon)
        client.enqueue(object : Callback<NewPostResponse> {
            override fun onResponse(
                call: Call<NewPostResponse>,
                response: Response<NewPostResponse>
            ) {
                _isError.value = true
                _isLoading.value = true
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error!!) {
                        _isError.value = false
                        _snackbarError.value = Event("onSuccess: ${responseBody.message}")
                    }
                } else {
                    _isError.value = true
                    _isLoading.value = false
                    Log.e(TAG, "onFailure: ${response.message()}")
                    _snackbarError.value = Event("onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<NewPostResponse>, t: Throwable) {
                _isError.value = true
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
                _snackbarError.value = Event("onFailure: ${t.message}")
            }
        })
    }
}