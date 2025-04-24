package com.example.gymbuddy.chat

data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String? = "",
    val channelId: String = "",
    val createdAt: Long = 0,
    val senderName: String = "",
    val receiverFcmToken: String = "",
    val imageUrl: String? = null,
    val shareWorkoutMessage: Boolean = false
)
