package com.example.gymbuddy.repository

import android.net.Uri
import com.example.gymbuddy.chat.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun listenForMessages(channelID: String): Flow<List<Message>>

    suspend fun sendImageMessage(
        uri: Uri,
        channelId: String,
        senderUsername: String,
        receiverFcmToken: String
    ): Result<Message> // zwraca URl
    suspend fun sendMessage(channelId: String, message: Message): Result<Message>
    suspend fun deleteAllMessages(channelID: String): Result<Boolean>
    suspend fun deleteAllImages(channelID: String): Result<Boolean>
    suspend fun updateLastMessage(channelId: String, lastMessage: String): Result<Boolean>
    suspend fun findChannel(
        currentUserId: String,
        otherUserId: String
    ): String? /* zwraca channelId */
}
