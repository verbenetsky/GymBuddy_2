package com.example.gymbuddy.repository

import com.example.gymbuddy.chat.ChatBotMessage
import kotlinx.coroutines.flow.Flow

interface ChatBotRepository {
    suspend fun deleteChatBotConversation(currentUserId: String): Result<Boolean>

    suspend fun sendMessage(
        userId: String,
        question: String,
        history: List<ChatBotMessage>
    ): Result<ChatBotMessage>

    fun observeConversation(userId: String): Flow<List<ChatBotMessage>>
}