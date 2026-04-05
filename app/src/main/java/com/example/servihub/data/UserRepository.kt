package com.example.servihub.data

import com.example.servihub.model.UserProfile
import kotlinx.coroutines.flow.Flow

class UserRepository(val userDao: UserDao) {

    val userProfile: Flow<UserProfile?> = userDao.getProfile()

    suspend fun insert(profile: UserProfile) {
        userDao.insertProfile(profile)
    }

    suspend fun delete() {
        userDao.clearProfile()
    }

    suspend fun update(profile: UserProfile) {
        userDao.updateProfile(profile)
    }

    val allProfessionals: Flow<List<UserProfile>> = userDao.getAllProfessionals()
}