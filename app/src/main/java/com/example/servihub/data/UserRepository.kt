package com.example.servihub.data

import com.example.servihub.model.Review
import com.example.servihub.model.UserProfile
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao, private val reviewDao: ReviewDao) {

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

    suspend fun getUserByEmail(email: String): UserProfile? = userDao.getUserByEmail(email)

    suspend fun login(email: String, password: String): UserProfile? = userDao.login(email, password)

    val allProfessionals: Flow<List<UserProfile>> = userDao.getAllProfessionals()

    val recentReviews: Flow<List<Review>> = reviewDao.getRecentReviews()

    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
    }
}