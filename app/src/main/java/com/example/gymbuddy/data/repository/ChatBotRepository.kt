package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.model.ChatBotMessage
import kotlinx.coroutines.flow.Flow

interface ChatBotRepository {
    suspend fun deleteChatBotConversation(currentUserId: String): Result<Boolean>

    suspend fun sendMessage(
        userId: String,
        question: String,
        history: List<ChatBotMessage>
    ): Result<Boolean>

    fun observeConversation(userId: String): Flow<List<ChatBotMessage>>
}