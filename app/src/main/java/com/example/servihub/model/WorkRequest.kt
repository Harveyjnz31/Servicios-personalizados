package com.example.servihub.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_requests")
data class WorkRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val specialty: String,
    val description: String,
    val location: String,
    val clientId: Int,
    val clientName: String,
    val budget: Double? = null,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
