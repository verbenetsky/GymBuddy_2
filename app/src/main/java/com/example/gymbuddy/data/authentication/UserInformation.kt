package com.example.gymbuddy.data.authentication

data class UserInformation(
    val userId: String = "",
    val fcmToken: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",
    val dateOfBirth: Long = 0,
    val hobbies: List<String> = emptyList(),
    val goal: String = "",
)

