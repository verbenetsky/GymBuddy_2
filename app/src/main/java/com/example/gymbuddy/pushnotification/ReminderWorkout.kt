package com.example.gymbuddy.pushnotification

import com.google.firebase.Timestamp

data class ReminderWorkoutDto(
    val workoutId: String,
    val fcmToken: String,
    val timeOfReminder: Timestamp,
    val messageText: String = ""
)
