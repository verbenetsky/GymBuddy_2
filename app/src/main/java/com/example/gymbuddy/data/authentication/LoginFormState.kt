package com.example.gymbuddy.data.authentication

data class LoginFormState(
    val email: String = "",
    val isEmailValid: Boolean = true,
    val password: String = "",
    val isPasswordValid: Boolean = true,
    val isLoginSuccessful: Boolean = true,
)




