package com.dicoding.dicodingstories.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.dicodingstories.DataDummy
import com.dicoding.dicodingstories.MainDispatcherRule
import com.dicoding.dicodingstories.data.paging.PostRepository
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.getOrAwaitValue
import com.dicoding.dicodingstories.ui.PostAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var postRepository: PostRepository

    @Test
    fun `when Get Post Should Not Null and Return Data`() = runTest {
        val dummyPost = DataDummy.generateDummyPostResponse()
        val data: PagingData<ListStoryItem> = PostPagingSource.snapshot(dummyPost)
        val expectedPost = MutableLiveData<PagingData<ListStoryItem>>()
        expectedPost.value = data
        Mockito.`when`(postRepository.getPost()).thenReturn(expectedPost)

        val mainViewModel = MainViewModel(postRepository)
        val actualPost: PagingData<ListStoryItem> = mainViewModel.listLivePost.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = PostAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualPost)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyPost.size, differ.snapshot().size)
        Assert.assertEquals(dummyPost[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Post Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<ListStoryItem>>()
        expectedQuote.value = data
        Mockito.`when`(postRepository.getPost()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(postRepository)
        val actualQuote: PagingData<ListStoryItem> = mainViewModel.listLivePost.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = PostAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

class PostPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

