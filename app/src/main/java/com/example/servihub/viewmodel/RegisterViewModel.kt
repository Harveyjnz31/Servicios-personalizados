package com.example.servihub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servihub.data.UserRepository
import com.example.servihub.model.UserProfile
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun registerUser(name: String, email: String, phone: String, service: String, exp: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || service.isBlank() || exp.isBlank()) {
            _error.value = "Por favor, completa todos los campos"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Correo electrónico no válido"
            return
        }

        val experience = exp.toIntOrNull() ?: 0
        val profile = UserProfile(
            fullName = name,
            email = email,
            phone = phone,
            serviceType = service,
            experienceYears = experience
        )
        
        viewModelScope.launch {
            try {
                repository.insert(profile)
                _registrationSuccess.postValue(true)
            } catch (e: Exception) {
                _error.postValue("Error al guardar: ${e.message}")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}