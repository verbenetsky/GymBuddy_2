package com.example.gymbuddy.data.authentication

data class SingInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
)
