package com.dicoding.dicodingstories.data.paging

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.dicodingstories.data.remote.ApiService
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.flow.first

class PostRepository(private val apiService: ApiService, private val pref : LoginPreference) {
    fun getPost(): LiveData<PagingData<ListStoryItem>>{
        Log.d("Repository", "Get post kepanggil")
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {PostPagingSource(apiService, pref)}
        ).liveData
    }
}