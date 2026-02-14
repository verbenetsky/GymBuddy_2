package com.example.gymbuddy.data.model

data class UserData(
    val userId: String = "",
    val email: String = "",
)
data class SignInValidation(
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isLoginSuccessful: Boolean = true,
)


