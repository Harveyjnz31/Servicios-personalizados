package com.example.servihub.data

import com.example.servihub.model.Review
import com.example.servihub.model.UserProfile
import com.example.servihub.model.WorkRequest
import com.example.servihub.model.Favorite
import com.example.servihub.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao, 
    private val reviewDao: ReviewDao,
    private val workRequestDao: WorkRequestDao,
    private val favoriteDao: FavoriteDao,
    private val chatDao: ChatDao
) {

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

    // Work Requests
    val allWorkRequests: Flow<List<WorkRequest>> = workRequestDao.getAllRequests()
    
    suspend fun insertWorkRequest(request: WorkRequest) {
        workRequestDao.insertRequest(request)
    }

    fun getCompletedWorkCount(): Flow<Int> = workRequestDao.getCompletedCount()

    // Favorites
    fun getFavorites(clientId: Int): Flow<List<Favorite>> = favoriteDao.getFavoritesByClient(clientId)
    
    suspend fun toggleFavorite(favorite: Favorite, isAdd: Boolean) {
        if (isAdd) favoriteDao.insertFavorite(favorite)
        else favoriteDao.removeFavorite(favorite.clientId, favorite.professionalId)
    }

    fun isFavorite(clientId: Int, proId: Int): Flow<Boolean> = favoriteDao.isFavorite(clientId, proId)

    // Chat
    fun getMessages(userId: Int, otherId: Int): Flow<List<ChatMessage>> = 
        chatDao.getMessagesBetweenUsers(userId, otherId)

    suspend fun sendMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    fun getConversations(userId: Int): Flow<List<ChatMessage>> = 
        chatDao.getConversationsForUser(userId)
}
