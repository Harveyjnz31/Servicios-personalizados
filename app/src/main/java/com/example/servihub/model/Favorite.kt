package com.example.servihub.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val professionalId: Int,
    val professionalName: String,
    val professionalSpecialty: String,
    val professionalRating: String,
    val professionalLocation: String
)
