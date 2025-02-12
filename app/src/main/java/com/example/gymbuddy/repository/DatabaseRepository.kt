package com.example.gymbuddy.repository

import com.example.gymbuddy.data.authentication.UserInformation

interface DatabaseRepository {
    suspend fun addUser(userInformation: UserInformation): Result<Boolean>
    suspend fun deleteUser(userId: String): Result<Boolean>
    suspend fun getUser(userId: String): Result<UserInformation>
    suspend fun addUsernameToDataBase(username: String): Result<Boolean>
    suspend fun deleteUsernameFromDataBase(username: String): Result<Boolean>
}