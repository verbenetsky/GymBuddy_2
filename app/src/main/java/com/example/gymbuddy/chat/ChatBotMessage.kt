package com.example.gymbuddy.chat

import java.util.Locale

data class ChatBotMessage(
    val message: String = "",
    val role: String = "",// user or model
    val createdAt: Long = System.currentTimeMillis()
)
