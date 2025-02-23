package com.example.gymbuddy.repository

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto

interface FriendRequestRepository {
    suspend fun addFriendRequestToDataBase(
        friendRequest: FriendRequestInformationDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ): Result<Boolean>

    suspend fun getAllFriendRequests(currentUserId: String): Result<List<FriendRequestInformationDto>>
    suspend fun getAllFullFriendsRequestInformation(listOfFriendRequest: List<FriendRequestInformationDto>): Result<List<UserFoundInformation>>

    suspend fun determineButtonState(searchedUserId: String): Result<String> // button state
    suspend fun addFriendsInformationToDatabase(
        currentUserId: String,
        friendId: String
    ): Result<Boolean>

    suspend fun deleteFriendRequestAfterAcceptingOrDecliningRequest(
        currentUserId: String,
        friendId: String
    ): Result<Boolean>

    suspend fun getAllFriend(currentUserId: String): Result<List<UserFoundInformation>>
    suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Boolean>
    //sprwadzamy zeby podczas wyszukiwania w Search Screen jesli juz mamy jakiegos usera w znajomych
    // to zeby nie wyswietlic opcji send request w wyszukiwaniu
    suspend fun checkFriendShip(friendId: String): Result<Boolean>
}