package com.example.gymbuddy.repository

interface AuthRepository {
    suspend fun logIn(email: String, password: String): Boolean
    suspend fun logOut(): Result<Boolean>
    suspend fun signUp(email: String, password: String): Boolean
    suspend fun signInWithCredentials(token: String):  Pair<Boolean,String?>
    suspend fun deleteUserAccount(): Result<Boolean>
    suspend fun sendResetPasswordEmail(email: String): Boolean
}