package com.dicoding.dicodingstories.ui.login

import android.util.Log
import androidx.lifecycle.*
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.LoginResponse
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel (private val pref : LoginPreference) :ViewModel() {

    companion object{
        private const val TAG = "LoginViewModel"
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError

    private val _isError = MutableLiveData<Boolean>()
    val isError : LiveData<Boolean> = _isError

    fun login(email:String, password: String){
        _isLoading.value = true
        val client = ApiConfig.getApiService().postLogin(email, password)
        client.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    viewModelScope.launch {
                        pref.login(LoginModel(true,response.body()?.loginResult?.token.toString()))
                    }
                    _isError.value = false
                    _snackbarError.value = Event("Login succeed")
                } else {
                    Log.e(TAG, "onDetailFailure: ${response.message()}")
                    _snackbarError.value = Event("onDetailFailure: ${response.message()}")
                    _isError.value = true
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onDetailFailure: ${t.message}")
                _snackbarError.value = Event("onDetailFailure: ${t.message}")
                _isError.value = true
            }

        })
    }

}