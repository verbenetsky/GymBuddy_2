package com.example.gymbuddy.chat


data class ChatBotMessage(
    val message: String = "",
    val role: String = "",// user or model
    val createdAt: Long = System.currentTimeMillis()
)
