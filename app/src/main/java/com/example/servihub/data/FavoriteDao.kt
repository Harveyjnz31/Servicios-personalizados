package com.example.servihub.data

import androidx.room.*
import com.example.servihub.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE clientId = :clientId")
    fun getFavoritesByClient(clientId: Int): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE clientId = :clientId AND professionalId = :proId)")
    fun isFavorite(clientId: Int, proId: Int): Flow<Boolean>

    @Query("DELETE FROM favorites WHERE clientId = :clientId AND professionalId = :proId")
    suspend fun removeFavorite(clientId: Int, proId: Int)
}
