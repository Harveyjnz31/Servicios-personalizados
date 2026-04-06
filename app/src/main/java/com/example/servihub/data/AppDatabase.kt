package com.example.servihub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.servihub.model.Review
import com.example.servihub.model.UserProfile
import com.example.servihub.model.WorkRequest
import com.example.servihub.model.Favorite
import com.example.servihub.model.ChatMessage

@Database(entities = [UserProfile::class, Review::class, WorkRequest::class, Favorite::class, ChatMessage::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao
    abstract fun workRequestDao(): WorkRequestDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "servihub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}