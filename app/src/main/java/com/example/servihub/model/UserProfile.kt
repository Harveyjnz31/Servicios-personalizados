package com.example.servihub.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val age: Int,
    val serviceType: String,
    val experienceYears: Int,
    val userRole: String = "CLIENT", // "CLIENT" or "PROFESSIONAL"
    val rating: Float = 0.0f
)
