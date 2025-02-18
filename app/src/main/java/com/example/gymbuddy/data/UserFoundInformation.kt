package com.example.gymbuddy.data

data class UserFoundInformation(
    val userID: String = "",
    val fcmToken: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val email: String = "",
    val dateOfBirth: Long = 0,
    val hobbies: List<String> = emptyList(),
    val goal: String = ""
)

