package com.example.gymbuddy.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.gymbuddy.R
import androidx.compose.ui.graphics.Color
import com.example.gymbuddy.workout.WorkoutState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


object CommonUtils {
    @Composable
    fun logoTheme(): Int {
        val isDarkTheme = isSystemInDarkTheme()
        return if (isDarkTheme) {
            R.drawable.logo_gymbuddy_black_theme
        } else {
            R.drawable.logo_gymbuddy_white_theme
        }
    }

//    fun compressImage(
//        context: Context,
//        imageUri: Uri,
//        maxWidth: Int = 1024,
//        maxHeight: Int = 1024,
//        quality: Int = 80
//    ): ByteArray? {
//        val resolver = context.contentResolver
//
//        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
//        resolver.openInputStream(imageUri)?.use { inputStream ->
//            BitmapFactory.decodeStream(inputStream, null, options)
//        }
//
//        var scale = 1
//        if (options.outWidth > maxWidth || options.outHeight > maxHeight) {
//            scale = Math.max(options.outWidth / maxWidth, options.outHeight / maxHeight)
//        }
//
//        val scaledOptions = BitmapFactory.Options().apply { inSampleSize = scale }
//        val bitmap: Bitmap? = resolver.openInputStream(imageUri)?.use { inputStream ->
//            BitmapFactory.decodeStream(inputStream, null, scaledOptions)
//        }
//
//        return bitmap?.let {
//            ByteArrayOutputStream().apply {
//                it.compress(Bitmap.CompressFormat.JPEG, quality, this)
//            }.toByteArray()
//

    fun longToString(num: Long): String {
        val minutesValue = num / 60 / 1000

        return if (minutesValue > 60) {
            val hours = minutesValue / 60
            val remainingMinutes = minutesValue % 60
            if (remainingMinutes == 0L) {
                val result = "${hours}h"
                result
            } else {
                "$hours h $remainingMinutes min"
            }
        } else {
            "${minutesValue}min"
        }
    }

    fun formatTimestamp(timestamp: Timestamp): String {
        val instant = timestamp.toDate().toInstant()
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm:ss a xxx")
            .withZone(ZoneId.of("Europe/Warsaw"))
        return formatter.format(instant)
    }


    fun workoutStateToMarkdown(workout: WorkoutState): String {
        // Funkcja pomocnicza do formatowania daty
        fun formatDate(ms: Long): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ms))
        }

        fun formatTimeOnly(ms: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
        }



        return buildString {
            appendLine("## Workout Summary")
            appendLine("- **Type**: ${workout.type}")
            appendLine("- **Workout Date**: ${formatDate(workout.workoutDate)}")
            appendLine("- **Start**: ${formatTimeOnly(workout.workoutStart)}")
            appendLine("- **End**: ${formatTimeOnly(workout.workoutEnd)}")

            // W zależności od typu treningu dodajemy specyficzne dane oraz listę ćwiczeń
            when (workout.type) {
                "Strength Workout" -> {
                    appendLine("- **Total Lifted (kg)**: ${workout.liftedKg}")
                    if (workout.listOfExercise.isNotEmpty()) {
                        appendLine("\n### Exercises:")
                        workout.listOfExercise.forEachIndexed { _, exercise ->
                            if (exercise.name != "") {
                                appendLine("**${exercise.name}** :")
                            }
                            if (exercise.listOfSets.isNotEmpty()) {
                                appendLine("")
                                exercise.listOfSets.forEachIndexed { i, set ->
                                    appendLine("     - Set ${i + 1}: ${set.kg} kg x ${set.reps} reps")
                                }
                            }
                        }
                    }
                }

                "HIT Workout" -> {
                    appendLine("- **HIIT Time**: ${workout.hitsLasted} sec")
                    if (workout.listOfExercise.isNotEmpty()) {
                        appendLine("\n### Exercises:")
                        workout.listOfExercise.forEachIndexed { index, exercise ->
                            if (exercise.name != "") {
                                appendLine("${index + 1}. **${exercise.name}**")
                            }
                            if (exercise.listOfHits.isNotEmpty()) {
                                appendLine("   - **HIT Rounds:**")
                                exercise.listOfHits.forEachIndexed { i, hit ->
                                    appendLine("     - Round ${i + 1}: ${hit.duration} sec work, ${hit.rest} sec rest")
                                }
                            }
                        }
                    }
                }

                "Cardio Workout" -> {
                    appendLine("- **Calories Burned**: ${workout.caloriesBurned}")
                    if (workout.listOfExercise.isNotEmpty()) {
                        appendLine("\n### Exercises:")
                        workout.listOfExercise.forEachIndexed { index, exercise ->
                            if (exercise.name != "") {
                                appendLine("${index + 1}. **${exercise.name}**")
                            }
                            if (exercise.listOfCardio.isNotEmpty()) {
                                appendLine("   - **Cardio Sessions:**")
                                exercise.listOfCardio.forEachIndexed { i, cardio ->
                                    appendLine("     - Session ${i + 1}: ${cardio.kcal} kcal, ${cardio.duration} min")
                                }
                            }
                        }
                    }
                }

                else -> {
                    appendLine("- **Total Lifted (kg)**: ${workout.liftedKg}")
                    appendLine("- **HIIT Time**: ${workout.hitsLasted} sec")
                    appendLine("- **Calories Burned**: ${workout.caloriesBurned}")
                }
            }
            appendLine("\n- **Status**: ${workout.status}")
        }
    }

    @Composable
    fun borderColor(): Color {
        return if (isSystemInDarkTheme()) {
            Color.White
        } else {
            Color.Black
        }
    }

    @Composable
    fun textGoogleButtonColor(): Color {
        return if (isSystemInDarkTheme()) {
            Color.White
        } else {
            Color.Black
        }
    }

}