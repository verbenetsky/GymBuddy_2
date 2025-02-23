package com.example.gymbuddy.friends

data class FriendRequestInformationDto(
    val receiverFcmToken: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val status: SendingRequestStatus = SendingRequestStatus.PENDING,
)

enum class SendingRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class FriendInformation(
    val friendId: String = "",
    val dateOfCreationOfFriendShip: Long = 0,
)