package com.example.servihub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.servihub.data.UserRepository
import com.example.servihub.model.Review
import com.example.servihub.model.UserProfile
import kotlinx.coroutines.launch

class WelcomeViewModel(private val repository: UserRepository) : ViewModel() {
    val userProfile: LiveData<UserProfile?> = repository.userProfile.asLiveData()

    val recentReviews: LiveData<List<Review>> = repository.recentReviews.asLiveData()

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

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(email, password)
            if (user != null) {
                // In a real app, we might just store the session. 
                // Here, we update the current profile in the DB to "log in"
                repository.insert(user)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}