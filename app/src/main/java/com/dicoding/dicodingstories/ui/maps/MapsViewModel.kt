package com.dicoding.dicodingstories.ui.maps

import android.util.Log
import androidx.lifecycle.*
import com.dicoding.dicodingstories.data.helper.Event
import com.dicoding.dicodingstories.data.remote.ApiConfig
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.remote.MapResponse
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import com.dicoding.dicodingstories.ui.main.MainViewModel
import com.dicoding.dicodingstories.ui.post.PostViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel (private val pref: LoginPreference): ViewModel() {

    private val _listLocPost = MutableLiveData<List<ListStoryItem>?>()
    val listLocPost : MutableLiveData<List<ListStoryItem>?> = _listLocPost

    private val _snackbarError = MutableLiveData<Event<String>>()
    val snackbarError : LiveData<Event<String>> = _snackbarError

    fun isLogin() : LiveData<LoginModel> {
        return pref.isLogin().asLiveData()
    }

    fun logout(){
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun getAllLocPost(header: String){
        val client = ApiConfig.getApiService().getPostLWithLoc("Bearer $header")
        client.enqueue(object : Callback<MapResponse>{
            override fun onResponse(call: Call<MapResponse>, response: Response<MapResponse>) {
                if (response.isSuccessful) {
                    _listLocPost.value =  response.body()?.listStory
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                    _snackbarError.value = Event("onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MapResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                _snackbarError.value = Event("onFailure: ${t.message}")
            }

        })
    }

    companion object{
        const val TAG = "MapsViewModel"
    }

}