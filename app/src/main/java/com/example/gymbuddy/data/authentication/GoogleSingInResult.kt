package com.example.gymbuddy.data.authentication

sealed class GoogleSignResult {
    data object Cancelled : GoogleSignResult()
    data class Success(val idToken: String) : GoogleSignResult()
    data object Failure : GoogleSignResult()
}