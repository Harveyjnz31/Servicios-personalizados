package com.example.servihub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.servihub.data.UserRepository
import com.example.servihub.model.UserProfile

class WelcomeViewModel(repository: UserRepository) : ViewModel() {
    val userProfile: LiveData<UserProfile?> = repository.userProfile.asLiveData()
}