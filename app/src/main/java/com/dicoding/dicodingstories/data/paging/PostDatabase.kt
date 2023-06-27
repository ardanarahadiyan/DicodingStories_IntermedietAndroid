package com.dicoding.dicodingstories.data.paging

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.dicodingstories.data.remote.ListStoryItem

@Database(
    entities = [ListStoryItem::class],
    version = 1,
    exportSchema = false
)

abstract class PostDatabase : RoomDatabase() {
    companion object{
        @Volatile
        private var INSTANCE: PostDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context):PostDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java, "post_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}