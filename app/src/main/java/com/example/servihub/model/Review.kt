package com.example.servihub.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val authorName: String,
    val content: String,
    val rating: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val professionalId: Int
)
