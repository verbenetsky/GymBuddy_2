package com.example.gymbuddy.data.model

data class AcceptOrDeclineOrRemoveFriendDto(
    val senderName: String,
    val receiverFcmToken: String
)

data class FriendRequestDto(
    val receiverFcmToken: String
)




