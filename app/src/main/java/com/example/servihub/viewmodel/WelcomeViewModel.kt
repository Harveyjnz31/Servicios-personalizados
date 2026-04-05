package com.example.servihub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.servihub.data.UserRepository
import com.example.servihub.model.UserProfile
import kotlinx.coroutines.launch

class WelcomeViewModel(private val repository: UserRepository) : ViewModel() {
    val userProfile: LiveData<UserProfile?> = repository.userProfile.asLiveData()

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

    fun login(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = repository.userDao.getProfileOnce()
            if (current != null && current.email.equals(email, ignoreCase = true)) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }
}