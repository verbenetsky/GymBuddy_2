package com.example.gymbuddy.workout

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class WorkoutViewModel @Inject constructor() : ViewModel() {

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (trening silowy)
    private val _strengthWorkoutState = MutableStateFlow(WorkoutState())
    val strengthWorkoutState: StateFlow<WorkoutState> = _strengthWorkoutState.asStateFlow()

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (cardio trening)
    private val _cardioWorkoutState = MutableStateFlow(WorkoutState())
    val cardioWorkoutState: StateFlow<WorkoutState> = _cardioWorkoutState.asStateFlow()

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (cardio trening)
    private val _hitWorkoutState = MutableStateFlow(WorkoutState())
    val hitWorkoutState: StateFlow<WorkoutState> = _hitWorkoutState.asStateFlow()

    // ---------------------------------------------------------------------------------------------

    private fun addSetOrCardioOrHitToExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        exerciseId: String,
        isCardio: Boolean,
    ) {

        val currentWorkout = workoutStateFlow.value

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                if (isCardio) {
                    val newCardio = CardioState()
                    exercise.copy(listOfCardio = exercise.listOfCardio + newCardio)
                } else {
                    val newSet = SetState()
                    exercise.copy(listOfSets = exercise.listOfSets + newSet)
                }
            } else {
                exercise
            }
        }
        workoutStateFlow.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun addCardioToExercise(exerciseId: String) {
        addSetOrCardioOrHitToExercise(_cardioWorkoutState, exerciseId, true)
    }

    fun addSetToExercise(exerciseId: String) {
        addSetOrCardioOrHitToExercise(_strengthWorkoutState, exerciseId, false)
    }

    // ---------------------------------------------------------------------------------------------

    private fun delete(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        isCardio: Boolean,
        cardioId: String = "",
        setId: String = "",
        exerciseId: String
    ) {
        val currentWorkout = workoutStateFlow.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                if (isCardio) {
                    val updatedCardios = exercise.listOfCardio.filter { it.id != cardioId }
                    exercise.copy(listOfCardio = updatedCardios)
                } else {
                    val updatedSets = exercise.listOfSets.filter { it.id != setId }
                    exercise.copy(listOfSets = updatedSets)
                }
            } else {
                exercise
            }
        }
        workoutStateFlow.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun deleteCardioFromExercise(cardioId: String, exerciseId: String) {
        delete(_cardioWorkoutState, true, cardioId, exerciseId = exerciseId)
    }

    fun deleteSetFromExercise(setId: String, exerciseId: String) {
        delete(_strengthWorkoutState,false, setId = setId, exerciseId = exerciseId)
    }

    // ---------------------------------------------------------------------------------------------

    fun updateKgInSet(exerciseId: String, setId: String, kg: String) {
        val currentWorkout = _strengthWorkoutState.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updateSets = exercise.listOfSets.map { set ->
                    if (set.id == setId) {
                        set.copy(kg = kg)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfSets = updateSets)
            } else {
                exercise
            }
        }
        _strengthWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun updateRepsInSet(exerciseId: String, setId: String, reps: String) {
        val currentWorkout = _strengthWorkoutState.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updateSets = exercise.listOfSets.map { set ->
                    if (set.id == setId) {
                        set.copy(reps = reps)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfSets = updateSets)
            } else {
                exercise
            }
        }
        _strengthWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    // ---------------------------------------------------------------------------------------------

    fun updateKcalInCardio(exerciseId: String, cardioId: String, kcal: String) {
        val currentWorkout = _cardioWorkoutState.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updateSets = exercise.listOfCardio.map { set ->
                    if (set.id == cardioId) {
                        set.copy(kcal = kcal)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfCardio = updateSets)
            } else {
                exercise
            }
        }
        _cardioWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun updateDurationInCardio(exerciseId: String, cardioId: String, duration: String) {
        val currentWorkout = _cardioWorkoutState.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updateCardio = exercise.listOfCardio.map { set ->
                    if (set.id == cardioId) {
                        set.copy(duration = duration)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfCardio = updateCardio)
            } else {
                exercise
            }
        }
        _cardioWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    // ---------------------------------------------------------------------------------------------

    private fun deleteExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        exerciseId: String
    ) {
        val currentWorkout = workoutStateFlow.value
        val updatedListOfExercise = currentWorkout.listOfExercise.filter { it.id != exerciseId }
        workoutStateFlow.update { currentState -> currentState.copy(listOfExercise = updatedListOfExercise) }
    }

    fun deleteStrengthExercise(exerciseId: String) {
        deleteExercise(_strengthWorkoutState, exerciseId)
    }

    fun deleteCardioExercise(exerciseId: String) {
        deleteExercise(_cardioWorkoutState, exerciseId)
    }

    // ---------------------------------------------------------------------------------------------

    private fun editNameForExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        newName: String,
        exerciseId: String
    ) {
        val currentWorkout = workoutStateFlow.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(name = newName)
            } else {
                exercise
            }
        }
        workoutStateFlow.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun editNameForCardioExercise(newExerciseName: String, exerciseId: String) {
        editNameForExercise(_cardioWorkoutState, newExerciseName, exerciseId)
    }

    fun editNameForStrengthExercise(newExerciseName: String, exerciseId: String) {
        editNameForExercise(_strengthWorkoutState, newExerciseName, exerciseId)
    }

    // ---------------------------------------------------------------------------------------------

    private fun addExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        newExerciseName: String
    ) {
        val newExercise = ExerciseState(name = newExerciseName)
        val currentWorkoutState = workoutStateFlow.value
        val updatedExercises = currentWorkoutState.listOfExercise + newExercise
        workoutStateFlow.value = currentWorkoutState.copy(listOfExercise = updatedExercises)
    }

    fun addExerciseForStrengthWorkout(newExerciseName: String) {
        addExercise(_strengthWorkoutState, newExerciseName)
    }

    fun addExerciseForCardioWorkout(newExerciseName: String) {
        addExercise(_cardioWorkoutState, newExerciseName)
    }

    // ---------------------------------------------------------------------------------------------
}