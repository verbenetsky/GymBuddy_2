package com.example.gymbuddy.repository

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.FriendRequestRepositoryImpl.FriendButtonState
import com.example.gymbuddy.friends.FriendRequestInformationDto
import kotlinx.coroutines.flow.Flow

interface FriendRequestRepository {

    suspend fun sendFriendRequest(friendRequest: FriendRequestInformationDto): Result<Boolean>
    suspend fun acceptFriendRequest(currentUserId: String, senderId: String): Result<Boolean>
    suspend fun declineFriendRequest(currentUserId: String, senderId: String): Result<Boolean>

    fun observeIncomingFriendRequests(currentUserId: String): Flow<List<UserFoundInformation>>
    fun observeButtonState(currentUserId: String, searchedUserId: String): Flow<FriendButtonState>
    fun observeFriends(currentUserId: String): Flow<List<UserFoundInformation>>

    suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Boolean> // usuwam znajomego
}