package com.example.gymbuddy.pushnotification

data class AcceptOrDeclineOrRemoveFriendDto(
    val senderName: String,
    val receiverFcmToken: String
)

data class FriendRequestDto(
    val receiverFcmToken: String
)




