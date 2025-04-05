package com.example.gymbuddy.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.pushnotification.ReminderWorkoutDto
import com.example.gymbuddy.utils.CommonUtils.longToString
import com.example.gymbuddy.utils.CommonUtils.formatTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor() : ViewModel() {

    private val db = Firebase.firestore
    private val currentUserUID = Firebase.auth.currentUser!!.uid

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (trening silowy)
    private val _strengthWorkoutState = MutableStateFlow(WorkoutState())
    val strengthWorkoutState: StateFlow<WorkoutState> = _strengthWorkoutState.asStateFlow()

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (cardio trening)
    private val _cardioWorkoutState = MutableStateFlow(WorkoutState())
    val cardioWorkoutState: StateFlow<WorkoutState> = _cardioWorkoutState.asStateFlow()

    // stan calego treningu, czyli ile cwiczen i nazwa treningu (cardio trening)
    private val _hitWorkoutState = MutableStateFlow(WorkoutState())
    val hitWorkoutState: StateFlow<WorkoutState> = _hitWorkoutState.asStateFlow()

    private val _listOfWorkouts = MutableStateFlow<List<WorkoutState>>(emptyList())
    val listOfWorkouts = _listOfWorkouts.asStateFlow()

    private val _workoutToEdit = MutableStateFlow(WorkoutState())
    val workoutToEdit: StateFlow<WorkoutState> = _workoutToEdit.asStateFlow()

    private val _workoutsState = MutableStateFlow<WorkoutsState>(WorkoutsState.Loading)
    val workoutsState = _workoutsState.asStateFlow()

    private var workoutsListenerRegistration: ListenerRegistration? = null

    // ---------------------------------------------------------------------------------------------

    init {
        listenForWorkouts()
    }

    // ogl jak korzystamy z korutyny to nie mieszamy style, czyli nie dajemy addOnSuccessListener
    // i addOnFailureListener, trzeba korzystac z metod opartych na suspend fun

    // OrderBy: Ascending, Descending. Po takich polach jak: duration, exercise, overall lifted
    // calories burned, hits lasted, date

    // jesli nie wybrano zadny konkrentny trening to mozna posortowac tylko po: duration, exercise, date

    // gdzie jesli wybrano Cardio Workout to mozna posortowac po: duration, exercise, calories burned, date
    // gdzie jesli wybrano HIT Workout to mozna posortowac po: duration, exercise, hits lasted, date
    // gdzie jesli wybrano Strength Workout to mozna posortowac po: duration, exercise, overall lifted, date

    fun listenForWorkouts(
        sortField: String? = null,
        direction: Query.Direction = Query.Direction.ASCENDING,
        filters: Map<String, Any> = emptyMap()
    ) {
        _workoutsState.value = WorkoutsState.Loading

        var query: Query = db.collection("workouts")
            .document(currentUserUID)
            .collection("workouts_of_user")

//        filters.forEach { (field, value) ->
//            query = query.whereEqualTo(field, value) // to nie dziala gdyz przekazujemy liste np. status: ["Planned"]
//        }                                            // a baza oczekuje pojedynczego elementu, czyli status: "Planned"

        filters.forEach { (field, value) ->
            query = if (value is List<*> && value.isNotEmpty()) { // sprawdzamy czy
                query.whereIn(field, value)
            } else {
                query.whereEqualTo(field, value)
            }
        }

        if (sortField != null) {
            query = query.orderBy(sortField, direction)
        }

        query.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                println("Error listening: $error")
                return@addSnapshotListener
            }
            val workouts = querySnapshot?.documents?.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(WorkoutState::class.java)
            } ?: emptyList()
            _listOfWorkouts.value = workouts
            _workoutsState.value = WorkoutsState.Loaded
        }
    }


    override fun onCleared() {
        super.onCleared()
        workoutsListenerRegistration?.remove()
    }

    // ---------------------------------------------------------------------------------------------

    fun removeWorkoutFromDb(workoutId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                println("usuwany wokrout o tym id : $workoutId")
                db.collection("workouts")
                    .document(currentUserUID)
                    .collection("workouts_of_user")
                    .document(workoutId)
                    .delete()
                    .await()
                onSuccess()
            } catch (e: Exception) {
                println("Error deleting workout: $e")
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun clearForm() {
        _hitWorkoutState.value = WorkoutState()
        _strengthWorkoutState.value = WorkoutState()
        _cardioWorkoutState.value = WorkoutState()
    }

    // ---------------------------------------------------------------------------------------------

    fun updateCurrentWorkout(
        workoutId: String,
        updateWorkout: WorkoutState,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                db.collection("workouts")
                    .document(currentUserUID)
                    .collection("workouts_of_user")
                    .document(workoutId)
                    .set(
                        updateWorkout,
                        SetOptions.merge()
                    ) // zastapi tylko te pola ktore sie zmienily
                    .await()
                onSuccess()
            } catch (e: Exception) {
                println("Something gone wrong: $e")
                onFailure()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun addWorkoutToList(workoutState: WorkoutState) {
        _listOfWorkouts.update { currentState -> currentState + workoutState }
    }

    // ---------------------------------------------------------------------------------------------

    // tutaj dodajemy do tymczasowej zmienne jeden workout zeby potem moc go wyswietlic i zaudejtowac
    fun tryToEditWorkout(workoutToEdit: WorkoutState) {
        _workoutToEdit.value = workoutToEdit
    }

    // ---------------------------------------------------------------------------------------------

    fun saveWorkoutToDb(workoutState: WorkoutState, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                db.collection("workouts")
                    .document(currentUserUID)
                    .collection("workouts_of_user")
                    .document(workoutState.id)
                    .set(workoutState)
                onSuccess()
                clearForm()
                addWorkoutToList(workoutState)
                println(workoutState)
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun addToDbReminder(
        listOfDates: List<Timestamp>,
        listOfHours: List<Long>,
        userFcmToken: String,
        workoutId: String,
        workoutState: WorkoutState,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {
            try {
                listOfDates.mapIndexed { index, date ->

                    println(formatTimestamp(date))

                    async {
                        db.collection("reminders")
                            .add(
                                ReminderWorkoutDto(
                                    fcmToken = userFcmToken,
                                    timeOfReminder = date,
                                    workoutId = workoutId,
                                    messageText = longToString(listOfHours[index])
                                )
                            ).await()
                    }
                }.awaitAll()
                onSuccess()
                saveWorkoutToDb(workoutState)
            } catch (e: Exception) {
                println("error during adding reminders: $e")
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun addElementToExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        exerciseId: String,
        elementType: ExerciseElementType
    ) {
        val currentWorkout = workoutStateFlow.value

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                when (elementType) {
                    ExerciseElementType.CARDIO -> {
                        val newCardio = CardioState()
                        exercise.copy(listOfCardio = exercise.listOfCardio + newCardio)
                    }

                    ExerciseElementType.SET -> {
                        val newSet = SetState()
                        exercise.copy(listOfSets = exercise.listOfSets + newSet)
                    }

                    ExerciseElementType.HIT -> {
                        val newHit = HitState()
                        exercise.copy(listOfHits = exercise.listOfHits + newHit)
                    }
                }
            } else {
                exercise
            }
        }
        workoutStateFlow.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun addCardioToExercise(exerciseId: String) {
        addElementToExercise(_cardioWorkoutState, exerciseId, ExerciseElementType.CARDIO)
    }

    fun addSetToExercise(exerciseId: String) {
        addElementToExercise(_strengthWorkoutState, exerciseId, ExerciseElementType.SET)
    }

    fun addHitToExercise(exerciseId: String) {
        addElementToExercise(_hitWorkoutState, exerciseId, ExerciseElementType.HIT)
    }

    fun addCardioToEditExercise(exerciseId: String) {
        addElementToExercise(_workoutToEdit, exerciseId, ExerciseElementType.CARDIO)
    }

    fun addSetToEditExercise(exerciseId: String) {
        addElementToExercise(_workoutToEdit, exerciseId, ExerciseElementType.SET)
    }

    fun addHitToEditExercise(exerciseId: String) {
        addElementToExercise(_workoutToEdit, exerciseId, ExerciseElementType.HIT)
    }

    // ---------------------------------------------------------------------------------------------

    private fun deleteElementFromExercise(
        workoutStateFlow: MutableStateFlow<WorkoutState>,
        exerciseId: String,
        elementType: ExerciseElementType,
        elementId: String
    ) {
        val currentWorkout = workoutStateFlow.value
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                when (elementType) {
                    ExerciseElementType.CARDIO -> {
                        val updatedCardios = exercise.listOfCardio.filter { it.id != elementId }
                        exercise.copy(listOfCardio = updatedCardios)
                    }

                    ExerciseElementType.SET -> {
                        val updatedSets = exercise.listOfSets.filter { it.id != elementId }
                        exercise.copy(listOfSets = updatedSets)
                    }

                    ExerciseElementType.HIT -> {
                        val updatedHits = exercise.listOfHits.filter { it.id != elementId }
                        exercise.copy(listOfHits = updatedHits)
                    }
                }
            } else {
                exercise
            }
        }
        workoutStateFlow.value = currentWorkout.copy(listOfExercise = updatedExercises)
    }

    fun deleteCardioFromExercise(cardioId: String, exerciseId: String) {
        deleteElementFromExercise(
            _cardioWorkoutState,
            exerciseId,
            ExerciseElementType.CARDIO,
            cardioId
        )
    }

    fun deleteSetFromExercise(setId: String, exerciseId: String) {
        deleteElementFromExercise(_strengthWorkoutState, exerciseId, ExerciseElementType.SET, setId)
    }

    fun deleteHitFromExercise(hitId: String, exerciseId: String) {
        deleteElementFromExercise(_hitWorkoutState, exerciseId, ExerciseElementType.HIT, hitId)
    }

    // *****************************************************************************************

    fun deleteCardioFromEditExercise(cardioId: String, exerciseId: String) {
        deleteElementFromExercise(_workoutToEdit, exerciseId, ExerciseElementType.CARDIO, cardioId)
    }

    fun deleteSetFromEditExercise(setId: String, exerciseId: String) {
        deleteElementFromExercise(_workoutToEdit, exerciseId, ExerciseElementType.SET, setId)
    }

    fun deleteHitFromEditExercise(hitId: String, exerciseId: String) {
        deleteElementFromExercise(_workoutToEdit, exerciseId, ExerciseElementType.HIT, hitId)
    }

    // *****************************************************************************************


    // ---------------------------------------------------------------------------------------------

    fun updateKgInSet(
        exerciseId: String,
        setId: String,
        kg: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _strengthWorkoutState.value
        }
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.listOfSets.map { set ->
                    if (set.id == setId) {
                        set.copy(kg = kg)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfSets = updatedSets)
            } else {
                exercise
            }
        }
        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _strengthWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
    }

    fun updateRepsInSet(
        exerciseId: String,
        setId: String,
        reps: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _strengthWorkoutState.value
        }
        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.listOfSets.map { set ->
                    if (set.id == setId) {
                        set.copy(reps = reps)
                    } else {
                        set
                    }
                }
                exercise.copy(listOfSets = updatedSets)
            } else {
                exercise
            }
        }
        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _strengthWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun updateKcalInCardio(
        exerciseId: String,
        cardioId: String,
        kcal: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _cardioWorkoutState.value
        }

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedCardio = exercise.listOfCardio.map { cardio ->
                    if (cardio.id == cardioId) {
                        cardio.copy(kcal = kcal)
                    } else {
                        cardio
                    }
                }
                exercise.copy(listOfCardio = updatedCardio)
            } else {
                exercise
            }
        }

        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _cardioWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
    }

    fun updateDurationInCardio(
        exerciseId: String,
        cardioId: String,
        duration: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _cardioWorkoutState.value
        }

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedCardio = exercise.listOfCardio.map { cardio ->
                    if (cardio.id == cardioId) {
                        cardio.copy(duration = duration)
                    } else {
                        cardio
                    }
                }
                exercise.copy(listOfCardio = updatedCardio)
            } else {
                exercise
            }
        }

        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _cardioWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun updateDurationInHit(
        exerciseId: String,
        hitId: String,
        duration: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _hitWorkoutState.value
        }

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedHits = exercise.listOfHits.map { hit ->
                    if (hit.id == hitId) {
                        hit.copy(duration = duration)
                    } else {
                        hit
                    }
                }
                exercise.copy(listOfHits = updatedHits)
            } else {
                exercise
            }
        }

        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _hitWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
    }

    fun updateRestInHit(
        exerciseId: String,
        hitId: String,
        rest: String,
        edit: Boolean = false
    ) {
        val currentWorkout = if (edit) {
            _workoutToEdit.value
        } else {
            _hitWorkoutState.value
        }

        val updatedExercises = currentWorkout.listOfExercise.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedHits = exercise.listOfHits.map { hit ->
                    if (hit.id == hitId) {
                        hit.copy(rest = rest)
                    } else {
                        hit
                    }
                }
                exercise.copy(listOfHits = updatedHits)
            } else {
                exercise
            }
        }

        if (edit) {
            _workoutToEdit.value = currentWorkout.copy(listOfExercise = updatedExercises)
        } else {
            _hitWorkoutState.value = currentWorkout.copy(listOfExercise = updatedExercises)
        }
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

    fun deleteHitExercise(exerciseId: String) {
        deleteExercise(_hitWorkoutState, exerciseId)
    }

    //tutaj usuwamy Exercise w ekranie edit
    fun deleteExerciseFromEdit(exerciseId: String) {
        deleteExercise(_workoutToEdit, exerciseId)
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

    fun editNameForHitExercise(newExerciseName: String, exerciseId: String) {
        editNameForExercise(_hitWorkoutState, newExerciseName, exerciseId)
    }

    // edytujemy name w edit workout screen
    fun editNameForEditExercise(newExerciseName: String, exerciseId: String) {
        editNameForExercise(_workoutToEdit, newExerciseName, exerciseId)
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

    fun addExerciseForHitWorkout(newExerciseName: String) {
        addExercise(_hitWorkoutState, newExerciseName)
    }

    // dodajemy Exercise kiedy edytujemy
    fun addExerciseForEditWorkout(newExerciseName: String) {
        addExercise(_workoutToEdit, newExerciseName)
    }

    // --------------------------Share with a friend workout----------------------------------------





    // ---------------------------------------------------------------------------------------------

    enum class ExerciseElementType {
        CARDIO,
        SET,
        HIT
    }

    sealed class WorkoutsState {
        data object Loading : WorkoutsState()
        data object Loaded : WorkoutsState()
    }
}