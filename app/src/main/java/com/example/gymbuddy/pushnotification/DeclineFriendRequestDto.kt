package com.example.gymbuddy.pushnotification

data class DeclineFriendRequestDto(
    val senderName: String,
    val receiverFcmToken: String
)