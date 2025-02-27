package com.example.gymbuddy.channel

data class Channel(
    val id: String = "",
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
