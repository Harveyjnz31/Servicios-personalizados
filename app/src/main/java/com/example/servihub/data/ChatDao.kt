package com.example.servihub.data

import androidx.room.*
import com.example.servihub.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId: Int, otherId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE receiverId = :userId GROUP BY senderId ORDER BY timestamp DESC")
    fun getConversationsForUser(userId: Int): Flow<List<ChatMessage>>
}
