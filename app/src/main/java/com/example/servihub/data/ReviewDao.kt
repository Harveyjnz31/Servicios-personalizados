package com.example.servihub.data

import androidx.room.*
import com.example.servihub.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC LIMIT 10")
    fun getRecentReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE professionalId = :professionalId ORDER BY timestamp DESC")
    fun getReviewsForProfessional(professionalId: Int): Flow<List<Review>>
}
