package com.example.gymbuddy.friends

data class FriendRequestInformationDto(
    val receiverId: String = "",
    val senderId: String = "",
    val date: Long = 0L,
    val status: SendingRequestStatus = SendingRequestStatus.PENDING,
)

enum class SendingRequestStatus {
    PENDING,
    DECLINED,
    ACCEPTED
}

//data class FriendInformation(
//    val friendId: String = "",
//    val dateOfCreationOfFriendShip: Long = 0,
//)