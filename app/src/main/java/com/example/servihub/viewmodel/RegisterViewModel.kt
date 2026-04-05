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

    fun registerUser(
        role: String,
        name: String,
        email: String,
        password: String,
        phone: String,
        age: String,
        city: String,
        address: String,
        service: String,
        exp: String
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank() || age.isBlank() || city.isBlank() || address.isBlank()) {
            _error.value = "Por favor, completa todos los campos obligatorios"
            return
        }

        if (password.length < 6) {
            _error.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        val ageInt = age.toIntOrNull()
        if (ageInt == null || ageInt < 18) {
            _error.value = "Debes ser mayor de 18 años para usar la app"
            return
        }

        if (role == "PROFESSIONAL" && (service.isBlank() || exp.isBlank())) {
            _error.value = "Por favor, completa los campos de profesional"
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
            password = password,
            phone = phone,
            age = ageInt,
            city = city,
            address = address,
            serviceType = if (role == "PROFESSIONAL") service else "",
            experienceYears = if (role == "PROFESSIONAL") experience else 0,
            userRole = role,
            rating = 5.0f // Initial rating
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