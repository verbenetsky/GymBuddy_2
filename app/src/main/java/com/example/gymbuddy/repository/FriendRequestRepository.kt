package com.example.gymbuddy.repository

import com.example.gymbuddy.friends.FriendRequestInformationDto

interface FriendRequestRepository {
    suspend fun acceptFriendRequest(senderId: String, receiverId: String): Result<Boolean>
    suspend fun declineFriendRequest(senderId: String, receiverId: String): Result<Boolean>
    suspend fun getFriendRequests(userId: String): Result<List<FriendRequestInformationDto>>
    suspend fun getFriends(userId: String): Result<List<String>>
}