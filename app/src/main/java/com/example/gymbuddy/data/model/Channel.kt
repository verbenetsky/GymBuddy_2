package com.example.gymbuddy.data.model

data class Channel(
    val id: String = "",
    val firstFriendId: String = "",
    val secondFriendId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
)
