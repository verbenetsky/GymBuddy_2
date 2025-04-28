package com.example.gymbuddy.data.authentication

sealed interface SignUpResult {
    data class Success(val email: String): SignUpResult
    data object Cancelled: SignUpResult
    data object Failure: SignUpResult
}