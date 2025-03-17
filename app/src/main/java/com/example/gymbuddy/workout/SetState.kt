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

data class ExerciseState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val listOfSets: List<SetState> = emptyList(),
    val listOfCardio: List<CardioState> = emptyList()
)

data class WorkoutState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val workOutTime: String = "",
    val listOfExercise: List<ExerciseState> = emptyList(),
    val status: WorkoutStatus = WorkoutStatus.PLANNED
)

enum class WorkoutStatus {
    PLANNED,
    IN_PROGRESS,
    FINISHED,
}
