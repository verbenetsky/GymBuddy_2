package com.example.gymbuddy.repository

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto

interface DatabaseRepository {
    suspend fun addUser(userInformation: UserInformation): Result<Boolean>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun getUser(userId: String): Result<UserFoundInformation>
    suspend fun addUsernameToDataBase(username: String): Result<Boolean>
    suspend fun deleteUsernameFromDataBase(username: String): Result<Boolean>
    suspend fun searchUser(username: String): Result<List<UserFoundInformation>>
    suspend fun addFcmTokenToDataBase(
        userId: String,
        token: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

}