package com.example.gymbuddy.friends

data class FriendRequestInformationDto(
    var receiverId: String = "",
)

enum class SendingRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}