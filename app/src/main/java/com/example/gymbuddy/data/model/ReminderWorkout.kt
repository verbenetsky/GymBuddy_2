package com.example.gymbuddy.data.model

import com.google.firebase.Timestamp

data class ReminderWorkoutDto(
    val workoutId: String,
    val fcmToken: String,
    val timeOfReminder: Timestamp,
    val messageText: String = ""
)
