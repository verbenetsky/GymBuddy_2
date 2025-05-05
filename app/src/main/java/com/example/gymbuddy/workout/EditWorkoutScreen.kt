package com.example.gymbuddy.workout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.gymbuddy.datasource.StatusOfWorkoutData.statusOfWorkout
import com.example.gymbuddy.datasource.WorkoutReminderOptions.workoutReminderOptions
import com.example.gymbuddy.ui.theme.surfaceDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    navigateToMyWorkoutsScreen: () -> Unit,
    innerNavController: NavController, workoutViewModel: WorkoutViewModel = hiltViewModel(
        innerNavController.getBackStackEntry("about_screen"),
    )
) {
    val workoutToEditState = workoutViewModel.workoutToEdit.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var workoutSaveToDb by remember { mutableStateOf<WorkoutState?>(null) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartDial by remember { mutableStateOf(false) }
    var showEndDial by remember { mutableStateOf(false) }

    val checkedStates =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(workoutReminderOptions.size) { false }) } }

    var statusWorkout by remember { mutableStateOf(workoutToEditState.value.status) } // locked
    val typeOfWorkout by remember { mutableStateOf(workoutToEditState.value.type) }

    val strengthWorkoutState = workoutViewModel.strengthWorkoutState.collectAsStateWithLifecycle()
    val cardioWorkoutState = workoutViewModel.cardioWorkoutState.collectAsStateWithLifecycle()
    val hitWorkoutState = workoutViewModel.hitWorkoutState.collectAsStateWithLifecycle()

    var strengthExerciseLimit by remember { mutableIntStateOf(workoutToEditState.value.listOfExercise.size) }
    var cardioExerciseLimit by remember { mutableIntStateOf(workoutToEditState.value.listOfExercise.size) }
    var hitExerciseLimit by remember { mutableIntStateOf(workoutToEditState.value.listOfExercise.size) }

    var selectedDate by remember { mutableLongStateOf(workoutToEditState.value.workoutDate) } // delete
    var selectedEndTime: TimePickerState? by remember {
        mutableStateOf(
            longToTimePicker(
                workoutToEditState.value.workoutEnd
            )
        )
    }
    var selectedStartTime: TimePickerState? by remember {
        mutableStateOf(
            longToTimePicker(
                workoutToEditState.value.workoutStart
            )
        )
    }

    var prevSizeEdit by remember { mutableIntStateOf(workoutToEditState.value.listOfExercise.size) }

    LaunchedEffect(workoutToEditState.value.listOfExercise.size) {
        if (workoutToEditState.value.listOfExercise.size > prevSizeEdit) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSizeEdit = hitWorkoutState.value.listOfExercise.size
    }

    LaunchedEffect(Unit) {
        println("workout to edit: ${workoutToEditState.value}")
        println("type is: $typeOfWorkout")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose status of your workout: ",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 17.sp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                statusOfWorkout.forEach { workout ->
                    FilterChip(
                        selected = statusWorkout == workout,
                        onClick = { statusWorkout = workout },
                        enabled = true,
                        label = {
                            Text(
                                text = workout,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = getWorkoutStatusText(statusWorkout),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 15.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (statusWorkout != "In Progress") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Select Date")
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar Icon"
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = if (selectedDate != 0L) dateConverter(selectedDate) else "")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Text(text = "Select Start Time")
                    IconButton(onClick = {
                        showStartDial = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Clock Icon"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = selectedStartTime?.let { timePickerStateToString(it) } ?: "")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Select End Time")
                    IconButton(onClick = {
                        showEndDial = true
                    }, enabled = selectedStartTime != null) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Clock Icon"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = selectedEndTime?.let { timePickerStateToString(it) } ?: "")
                }

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalDivider(thickness = 2.dp)

            }
        }

        if (typeOfWorkout == "Strength Workout") {

            workoutToEditState.value.listOfExercise.forEachIndexed { index, exercise ->
                key(exercise.id) {

                    var localTempName by remember { mutableStateOf(exercise.name) }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Exercise nr: ${index + 1}",
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            TextField(value = localTempName,
                                onValueChange = { newValue ->
                                    localTempName = newValue
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleSmall.copy(
                                    color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else surfaceDark,
                                    fontSize = 12.sp, textAlign = TextAlign.Center
                                ),
                                keyboardActions = KeyboardActions(onDone = { // tu konieczne jest nacisniecie przycisku Done bo bez niego czemus duplikuja sie recordy treningowe jak sie klika Save zeby zapisac caly trening po edycji, z poziomu telefonu cieakc
                                    workoutViewModel.editNameForEditExercise(
                                        localTempName,
                                        exercise.id
                                    )
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                ),
                                // todo problem jest taki ze jesli zmienia nazwe exercise ale nie klikna done albo nie zamkne klawiature i klikna na continue to dubluja sie recordy, do poprawy w przyszlosci
                                modifier = Modifier
                                    .width(200.dp)
                                    .offset(x = (-15).dp)
                                    .onFocusChanged { focusState ->
                                        if (!focusState.isFocused) {
                                            workoutViewModel.editNameForEditExercise(
                                                localTempName,
                                                exercise.id
                                            )
                                        } else {
                                            localTempName = ""
                                        }
                                    })

                            IconButton(onClick = {
                                workoutViewModel.deleteExerciseFromEdit(exercise.id)
                                strengthExerciseLimit--
                                Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                    .show()

                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete, contentDescription = null
                                )
                            }
                        }
                    }
                }

                // Lista zestawów dla danego ćwiczenia
                exercise.listOfSets.forEachIndexed { i, set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Set nr: ${i + 1}", style = MaterialTheme.typography.titleSmall
                        )

                        // Pole BasicTextField dla kg
                        BasicTextField(
                            value = set.kg,
                            onValueChange = {
                                workoutViewModel.updateKgInSet(exercise.id, set.id, it, true)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "kg", style = MaterialTheme.typography.bodyMedium
                        )

                        // Pole BasicTextField dla reps
                        BasicTextField(
                            value = set.reps,
                            onValueChange = {
                                workoutViewModel.updateRepsInSet(exercise.id, set.id, it, true)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "reps.", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteSetFromEditExercise(set.id, exercise.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "delete set from exercise"
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (workoutToEditState.value.listOfExercise.lastIndex == index) {
                            coroutineScope.launch {
                                delay(100)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        workoutViewModel.addSetToEditExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a set")
                }
            }


            Button(
                onClick = {
                    workoutViewModel.addExerciseForEditWorkout("")
                    strengthExerciseLimit++
                },
                shape = RoundedCornerShape(4.dp),
                enabled = if (strengthExerciseLimit == 20) false else true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Add an exercise ($strengthExerciseLimit/20)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(onClick = {
                val updatedWorkout = workoutToEditState.value.copy(
                    type = "Strength Workout",
                    exerciseCount = workoutToEditState.value.listOfExercise.size,
                    liftedKg = workoutToEditState.value.listOfExercise.sumOf { exercise ->
                        exercise.listOfSets.sumOf { set ->
                            set.reps.toInt() * set.kg.toInt()
                        }
                    },
                    userId = Firebase.auth.currentUser!!.uid,
                    workoutDate = if (statusWorkout == "In Progress") System.currentTimeMillis() else selectedDate,
                    workoutStart = timePickerToLong(selectedStartTime!!),
                    workoutEnd = timePickerToLong(selectedEndTime!!),
                    workoutTime = calculateWorkoutDuration(
                        selectedStartTime!!,
                        selectedEndTime!!
                    ),
                    status = statusWorkout
                )
                if (statusWorkout == "Planned") {
                    workoutSaveToDb = updatedWorkout
                    showBottomSheet = true
                } else {
                    workoutViewModel.updateCurrentWorkout(updatedWorkout.id, updatedWorkout, {
                        navigateToMyWorkoutsScreen()
                        Toast.makeText(
                            context,
                            "Workout successfully updated.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, {
                        Toast.makeText(
                            context,
                            "Something gone wrong!",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            },
                shape = RoundedCornerShape(4.dp),
                enabled =
                workoutToEditState.value.listOfExercise.all { it.listOfSets.isNotEmpty() }
                        && workoutToEditState.value.listOfExercise.isNotEmpty()
                        && workoutToEditState.value.listOfExercise.all { it.listOfSets.all { it.kg != "" } }
                        && workoutToEditState.value.listOfExercise.all { it.listOfSets.all { it.reps != "" } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)

            ) {
                Text(
                    text = if (statusWorkout == "Planned") "Continue" else "Save Workout",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }

        if (typeOfWorkout == "Cardio Workout") {

            workoutToEditState.value.listOfExercise.forEachIndexed { index, exercise ->
                key(exercise.id) {

                    var localTempName by remember { mutableStateOf(exercise.name) }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Exercise nr: ${index + 1}",
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            TextField(value = localTempName,
                                onValueChange = { newValue ->
                                    localTempName = newValue
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleSmall.copy(
                                    color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else surfaceDark,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    workoutViewModel.editNameForEditExercise(
                                        localTempName, exercise.id
                                    )
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = if (isSystemInDarkTheme()) Color.Red else Color.Red,
                                    unfocusedTextColor = if (isSystemInDarkTheme()) Color.Red else Color.Red,
                                    disabledTextColor = if (isSystemInDarkTheme()) Color.Red else Color.Red,
                                    focusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                ),
                                modifier = Modifier
                                    .width(200.dp)
                                    .offset(x = (-15).dp)
                                    .onFocusChanged { focusState ->
                                        if (!focusState.isFocused) {
                                            workoutViewModel.editNameForEditExercise(
                                                localTempName,
                                                exercise.id
                                            )
                                        } else {
                                            localTempName = ""
                                        }
                                    })

                            IconButton(onClick = {
                                workoutViewModel.deleteExerciseFromEdit(exercise.id)
                                cardioExerciseLimit--
                                Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                    .show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete, contentDescription = null
                                )
                            }
                        }
                    }
                }

                // Lista zestawów cardio dla danego ćwiczenia
                exercise.listOfCardio.forEachIndexed { i, cardio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cardio ${i + 1}: burned ",
                            style = MaterialTheme.typography.titleSmall
                        )

                        // Pole BasicTextField dla kcal
                        BasicTextField(
                            value = cardio.kcal,
                            onValueChange = {
                                workoutViewModel.updateKcalInCardio(
                                    exercise.id,
                                    cardio.id,
                                    it,
                                    true
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = "kcal, it took", style = MaterialTheme.typography.bodyMedium
                        )

                        // Pole BasicTextField dla duration
                        BasicTextField(
                            value = cardio.duration,
                            onValueChange = {
                                workoutViewModel.updateDurationInCardio(
                                    exercise.id,
                                    cardio.id,
                                    it,
                                    true
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = ".", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteCardioFromEditExercise(cardio.id, exercise.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (cardioWorkoutState.value.listOfExercise.lastIndex == index) {
                            coroutineScope.launch {
                                delay(100)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        workoutViewModel.addCardioToEditExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a cardio")
                }
            }

            Button(
                onClick = {
                    workoutViewModel.addExerciseForEditWorkout("")
                    cardioExerciseLimit++
                },
                shape = RoundedCornerShape(4.dp),
                enabled = if (cardioExerciseLimit == 20) false else true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Add an exercise ($cardioExerciseLimit/20)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Button(onClick = {
                val updatedWorkout = workoutToEditState.value.copy(
                    type = "Cardio Workout",
                    exerciseCount = workoutToEditState.value.listOfExercise.size,
                    caloriesBurned = workoutToEditState.value.listOfExercise.sumOf { exercise ->
                        exercise.listOfCardio.sumOf { it.kcal.toIntOrNull() ?: 0 }
                    },
                    userId = Firebase.auth.currentUser!!.uid,
                    workoutDate = if (statusWorkout == "In Progress") System.currentTimeMillis() else selectedDate,
                    workoutStart = timePickerToLong(selectedStartTime!!),
                    workoutEnd = timePickerToLong(selectedEndTime!!),
                    workoutTime = calculateWorkoutDuration(
                        selectedStartTime!!,
                        selectedEndTime!!
                    ),
                    status = statusWorkout
                )
                if (statusWorkout == "Planned") {
                    workoutSaveToDb = updatedWorkout
                    showBottomSheet = true
                } else {
                    workoutViewModel.updateCurrentWorkout(updatedWorkout.id, updatedWorkout, {
                        navigateToMyWorkoutsScreen()
                        Toast.makeText(
                            context,
                            "Workout successfully updated.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, {
                        Toast.makeText(
                            context,
                            "Something gone wrong!",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }

            },
                shape = RoundedCornerShape(4.dp),
                enabled =
                workoutToEditState.value.listOfExercise.all { it.listOfCardio.isNotEmpty() }
                        && workoutToEditState.value.listOfExercise.isNotEmpty()
                        && workoutToEditState.value.listOfExercise.all { it.listOfCardio.all { it.kcal != "" } }
                        && workoutToEditState.value.listOfExercise.all { it.listOfCardio.all { it.duration != "" } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)

            ) {
                Text(
                    text = if (statusWorkout == "Planned") "Continue" else "Save Workout",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }

        if (typeOfWorkout == "HIT Workout") {

            workoutToEditState.value.listOfExercise.forEachIndexed { index, exercise ->
                key(exercise.id) {

                    var localTempName by remember { mutableStateOf(exercise.name) }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Exercise nr: ${index + 1}",
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            TextField(value = localTempName,
                                onValueChange = { newValue ->
                                    localTempName = newValue
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleSmall.copy(
                                    color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else surfaceDark,
                                    fontSize = 12.sp, textAlign = TextAlign.Center
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    workoutViewModel.editNameForEditExercise(
                                        localTempName, exercise.id
                                    )
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                    unfocusedIndicatorColor = if (isSystemInDarkTheme()) surfaceDark else MaterialTheme.colorScheme.surface,
                                ),
                                modifier = Modifier
                                    .width(200.dp)
                                    .offset(x = (-15).dp)
                                    .onFocusChanged { focusState ->
                                        if (!focusState.isFocused) {
                                            workoutViewModel.editNameForEditExercise(
                                                localTempName,
                                                exercise.id
                                            )
                                        } else {
                                            localTempName = ""
                                        }
                                    })

                            IconButton(onClick = {
                                workoutViewModel.deleteExerciseFromEdit(exercise.id)
                                hitExerciseLimit--
                                Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                    .show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete, contentDescription = null
                                )
                            }
                        }
                    }
                }

                exercise.listOfHits.forEachIndexed { i, hit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Duration:",
                            style = MaterialTheme.typography.titleSmall
                        )

                        // Pole BasicTextField dla duration
                        BasicTextField(
                            value = hit.duration,
                            onValueChange = {
                                workoutViewModel.updateDurationInHit(exercise.id, hit.id, it, true)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = "sec, rest", style = MaterialTheme.typography.bodyMedium
                        )

                        // Pole BasicTextField dla rest
                        BasicTextField(
                            value = hit.rest,
                            onValueChange = {
                                workoutViewModel.updateRestInHit(exercise.id, hit.id, it, true)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                textAlign = TextAlign.Center,
                                color = if (isSystemInDarkTheme()) Color(0xFFFFF1E5) else Color(
                                    0xFF18120B
                                )
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = "sec.", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteHitFromEditExercise(hit.id, exercise.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (workoutToEditState.value.listOfExercise.lastIndex == index) {
                            coroutineScope.launch {
                                delay(100)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        workoutViewModel.addHitToEditExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a HIT")
                }
            }

            Button(
                onClick = {
                    workoutViewModel.addExerciseForEditWorkout("")
                    hitExerciseLimit++
                },
                shape = RoundedCornerShape(10.dp),
                enabled = if (hitExerciseLimit == 20) false else true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Add an exercise ($hitExerciseLimit/20)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Button(onClick = {
                val updatedWorkout = workoutToEditState.value.copy(
                    type = "HIT Workout",
                    exerciseCount = workoutToEditState.value.listOfExercise.size,
                    hitsLasted = workoutToEditState.value.listOfExercise.sumOf { exercise ->
                        exercise.listOfHits.sumOf { it.duration.toIntOrNull() ?: 0 }
                    },
                    userId = Firebase.auth.currentUser!!.uid,
                    workoutDate = if (statusWorkout == "In Progress") System.currentTimeMillis() else selectedDate,
                    workoutStart = timePickerToLong(selectedStartTime!!),
                    workoutEnd = timePickerToLong(selectedEndTime!!),
                    workoutTime = calculateWorkoutDuration(
                        selectedStartTime!!,
                        selectedEndTime!!
                    ),
                    status = statusWorkout
                )
                if (statusWorkout == "Planned") {
                    workoutSaveToDb = updatedWorkout
                    showBottomSheet = true
                } else {
                    workoutViewModel.updateCurrentWorkout(updatedWorkout.id, updatedWorkout, {
                        navigateToMyWorkoutsScreen()
                        Toast.makeText(
                            context,
                            "Workout successfully updated.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, {
                        Toast.makeText(
                            context,
                            "Something gone wrong!",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            },
                shape = RoundedCornerShape(4.dp),
                enabled = workoutToEditState.value.listOfExercise.all { it.listOfHits.isNotEmpty() }
                        && workoutToEditState.value.listOfExercise.isNotEmpty()
                        && workoutToEditState.value.listOfExercise.all { it.listOfHits.all { it.duration != "" } }
                        && workoutToEditState.value.listOfExercise.all { it.listOfHits.all { it.rest != "" } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = if (statusWorkout == "Planned") "Continue" else "Save Workout",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    }

    if (showDatePicker) {
        DatePickerModal2(
            onDateSelected = { date ->
                showDatePicker = false
                if (date != null) {
                    selectedDate = date
                }
            },
            onDismiss = { showDatePicker = false },
            finishedWorkout = statusWorkout == "Finished"
        )
    }
    if (showStartDial) {
        TimeSelection(
            onDismiss = {
                showStartDial = false
            },
            onConfirm = { time ->
                selectedStartTime = time
                showStartDial = false
            },
            startTime = null,
        )
    }
    if (showEndDial) {
        TimeSelection(
            onDismiss = {
                showEndDial = false
            },
            onConfirm = { time ->
                selectedEndTime = time
                showEndDial = false
            },
            startTime = selectedStartTime,
        )
    }

    if (showBottomSheet) {
        ModalBottomSheetNotification(
            stateList = checkedStates,
            sheetState = sheetState,
            sheetStateChange = { newValue -> showBottomSheet = newValue },
            onNoClick = {
                workoutSaveToDb?.let {
                    workoutViewModel.saveWorkoutToDb(
                        workoutState = it,
                        onSuccess = {
                            showBottomSheet = false
                            navigateToMyWorkoutsScreen()
                            Toast.makeText(
                                context,
                                "Workout successfully updated.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        })
                }
            },
            onSetNotificationClick = {

            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun longToTimePicker(longTime: Long): TimePickerState {
    val totalMinutes = (longTime / 60000L).toInt()
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    return object : TimePickerState {
        override var hour: Int = hour
        override var minute: Int = minute
        override var selection: TimePickerSelectionMode = TimePickerSelectionMode.Hour
        override var is24hour: Boolean = true
    }
}