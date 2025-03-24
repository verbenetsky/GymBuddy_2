package com.example.gymbuddy.datasource

object SortAndFilterOption {
    val sortingOptions = listOf(
        "Date from old to new",
        "Date from new to old",
        "Amount of exercise descending",
        "Amount of exercise ascending",
        "Duration ascending",
        "Duration descending"
    )



    val additionalSortingOptionCardioWorkout =
        listOf("Calories burned ascending", "Calories burned descending")

    val additionalSortingOptionStrengthWorkout =
        listOf("Overall lifted ascending", "Overall lifted descending")

    val additionalSortingOptionHITWorkout =
        listOf("HITs lasted descending", "HITs lasted ascending")

    val typeOptions = listOf(
        "Strength Workout",
        "HIT Workout",
        "Cardio Workout",
    )

    val statusOptions = listOf(
        "Finished",
        "Planned",
        "In Progress",
    )
}
