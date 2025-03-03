package com.example.gymbuddy.chat

data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String? = "",
    val channelId: String = "",
    val createdAt: String = "",
    val senderName: String = "",
    val imageUrl: String? = null,
    val senderImage: String? = null,
)
