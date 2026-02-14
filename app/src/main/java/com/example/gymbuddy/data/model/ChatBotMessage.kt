package com.example.gymbuddy.data.model


data class ChatBotMessage(
    val message: String = "",
    val role: String = "",// user or model
    val createdAt: Long = System.currentTimeMillis()
)
