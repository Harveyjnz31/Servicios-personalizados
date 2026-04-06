package com.example.servihub.data

import androidx.room.*
import com.example.servihub.model.WorkRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRequestDao {
    @Query("SELECT * FROM work_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<WorkRequest>>

    @Query("SELECT * FROM work_requests WHERE specialty = :specialty ORDER BY createdAt DESC")
    fun getRequestsBySpecialty(specialty: String): Flow<List<WorkRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: WorkRequest)

    @Delete
    suspend fun deleteRequest(request: WorkRequest)
    
    @Query("SELECT COUNT(*) FROM work_requests WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>
}
