package com.example.servihub.data

import androidx.room.*
import com.example.servihub.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    @Update
    suspend fun updateProfile(profile: UserProfile)
}