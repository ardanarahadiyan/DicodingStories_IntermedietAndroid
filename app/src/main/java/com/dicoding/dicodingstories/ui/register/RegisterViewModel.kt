package com.dicoding.dicodingstories.ui.register

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel (application: Application) : ViewModel() {

    companion object{
        private val TAG = "RegisterViewModel"
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError : LiveData<Boolean> = _isError

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError

    fun register(name:String, email: String, password: String){
        _isLoading.value = true
        _isError.value = true
        val client = ApiConfig.getApiService().postRegister(name, email, password)
        client.enqueue(object : Callback<RegisterResponse>{
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    Log.e(TAG, "onSuccess: ${response.body()?.message}")
                    _isError.value = false
                }else{
                    Log.e(TAG, "onDetailFailure: ${response.message()}")
                    _isError.value = true
                    _snackbarError.value = Event("onDetailFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onDetailFailure: ${t.message}")
                _isError.value = true
                _snackbarError.value = Event("onDetailFailure: ${t.message}")
            }

        })
    }


}