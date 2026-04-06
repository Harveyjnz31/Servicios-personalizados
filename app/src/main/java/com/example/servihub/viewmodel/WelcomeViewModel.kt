package com.example.servihub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.servihub.data.UserRepository
import com.example.servihub.model.Review
import com.example.servihub.model.UserProfile
import com.example.servihub.model.WorkRequest
import com.example.servihub.model.Favorite
import com.example.servihub.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WelcomeViewModel(private val repository: UserRepository) : ViewModel() {
    val userProfile: LiveData<UserProfile?> = repository.userProfile.asLiveData()

    fun insertReview(review: Review) {
        viewModelScope.launch {
            repository.insertReview(review)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.delete()
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insert(profile)
        }
    }

    val allProfessionals: LiveData<List<UserProfile>> = repository.allProfessionals.asLiveData()

    val allWorkRequests: LiveData<List<WorkRequest>> = repository.allWorkRequests.asLiveData()

    fun insertWorkRequest(request: WorkRequest) {
        viewModelScope.launch {
            repository.insertWorkRequest(request)
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(email, password)
            if (user != null) {
                repository.insert(user)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // Favorites
    fun getFavorites(clientId: Int): LiveData<List<Favorite>> = 
        repository.getFavorites(clientId).asLiveData()

    fun toggleFavorite(favorite: Favorite, isAdd: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(favorite, isAdd)
        }
    }

    fun isFavorite(clientId: Int, proId: Int): LiveData<Boolean> =
        repository.isFavorite(clientId, proId).asLiveData()

    // Chat
    fun getMessages(userId: Int, otherId: Int): LiveData<List<ChatMessage>> =
        repository.getMessages(userId, otherId).asLiveData()

    fun sendMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun getConversations(userId: Int): LiveData<List<ChatMessage>> =
        repository.getConversations(userId).asLiveData()

    val recentReviews: LiveData<List<Review>> = repository.recentReviews.asLiveData()
}
