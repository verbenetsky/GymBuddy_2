package com.example.gymbuddy.data.authentication

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String = "",
    val email: String = "",
    val username: String? = null,
    val profilePictureUrl: String? = null
)
data class SignInValidation(
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isLoginSuccessful: Boolean = true,
)


