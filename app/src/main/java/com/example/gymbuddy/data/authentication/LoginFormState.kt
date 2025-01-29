package com.example.gymbuddy.data.authentication

data class LoginFormState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val isLoginSuccessful: Boolean = true,
)




