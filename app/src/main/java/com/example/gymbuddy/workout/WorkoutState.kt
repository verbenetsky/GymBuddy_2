package com.example.gymbuddy.workout

import java.util.UUID

data class SetState(
    val id: String = UUID.randomUUID().toString(),
    val kg: String = "",
    val reps: String = ""
)

data class CardioState(
    val id: String = UUID.randomUUID().toString(),
    val kcal: String = "",
    val duration: String = ""
)

data class HitState(
    val id: String = UUID.randomUUID().toString(),
    val rest: String = "",
    val duration: String = ""
)

data class ExerciseState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val listOfSets: List<SetState> = emptyList(),
    val listOfCardio: List<CardioState> = emptyList(),
    val listOfHits: List<HitState> = emptyList()
)

data class WorkoutState(
    val userId: String = "",
    val id: String = UUID.randomUUID().toString(),
    val type: String = "",
    val exerciseCount: Int = 0,
    val caloriesBurned: Int = 0,
    val liftedKg: Int = 0,
    val hitsLasted: Int = 0,
    val workoutDate: Long = 0,
    val workoutTime: Long = 0,
    val workoutStart: Long = 0,
    val workoutEnd: Long = 0,
    val listOfExercise: List<ExerciseState> = emptyList(),
    val status: String = "",
)



