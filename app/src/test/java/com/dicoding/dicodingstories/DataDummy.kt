package com.dicoding.dicodingstories

import com.dicoding.dicodingstories.data.remote.ListStoryItem

object DataDummy {
    fun generateDummyPostResponse(): List<ListStoryItem>{
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100){
            val postItem = ListStoryItem(
                "photoUrl $i",
                "createdAt $i",
                "NAME $i",
                "Description $i",
                i.toFloat(),
                i.toString(),
                i.toFloat()
            )
            items.add(postItem)
        }
        return items
    }
}