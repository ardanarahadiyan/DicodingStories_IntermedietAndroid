package com.dicoding.dicodingstories.data.paging

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.dicodingstories.data.remote.ApiService
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.data.remote.PostResponse
import com.dicoding.dicodingstories.data.user.LoginModel
import com.dicoding.dicodingstories.data.user.LoginPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.take
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Header

class PostPagingSource (private val apiService: ApiService, private val pref: LoginPreference): PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = pref.isLogin().first().token

            if (token.isNotEmpty()){
                val responseData = apiService.getAllPost("Bearer $token", position, params.loadSize)
                Log.d("Paging Source", "Token: $token response body ${responseData.body()}")
                LoadResult.Page(
                    data = responseData.body()?.listStory ?: emptyList(),
                    prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                    nextKey = if (responseData.body()?.listStory!!.isEmpty()) null else position + 1)
            }else{
                Log.d("Paging Source", "Token kosong nih")
                LoadResult.Error(Exception("Failed"))
            }

        } catch (exception : Exception){
            Log.d("Exception", "${exception.message}, ${exception.cause}")
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }


}