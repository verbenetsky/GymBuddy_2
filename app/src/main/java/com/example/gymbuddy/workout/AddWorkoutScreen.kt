package com.example.gymbuddy.workout

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.datasource.StatusOfWorkoutData.statusOfWorkout
import com.example.gymbuddy.datasource.TypeOfWorkoutData.TypeOfWorkout
import com.example.gymbuddy.datasource.WorkoutReminderOptions.workoutReminderOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    innerNavController: NavController,
    navigateToMyWorkoutsScreen: () -> Unit,
    userManagementViewModel: UserManagementViewModel,
    workoutViewModel: WorkoutViewModel = hiltViewModel(
        innerNavController.getBackStackEntry("about_screen")
    )
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val userInformationState = userManagementViewModel.userInformationState.collectAsState()

    var workoutSaveToDb by remember { mutableStateOf<WorkoutState?>(null) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartDial by remember { mutableStateOf(false) }
    var showEndDial by remember { mutableStateOf(false) }

    val checkedStates =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(workoutReminderOptions.size) { false }) } }

    var reminderWorkoutState by remember { mutableStateOf(WorkoutState()) }

    // 0 - 1hour
    // 1 - 2hour
    // 2 - 4hour
    // 3 - 8hour
    // 4 - 12hour
    // 5 - 24hour

    var statusWorkout by remember { mutableStateOf("") }
    var typeOfWorkout by remember { mutableStateOf("") }

    val strengthWorkoutState = workoutViewModel.strengthWorkoutState.collectAsStateWithLifecycle()
    val cardioWorkoutState = workoutViewModel.cardioWorkoutState.collectAsStateWithLifecycle()
    val hitWorkoutState = workoutViewModel.hitWorkoutState.collectAsStateWithLifecycle()

    var strengthExerciseLimit by remember { mutableIntStateOf(0) }
    var cardioExerciseLimit by remember { mutableIntStateOf(0) }
    var hitExerciseLimit by remember { mutableIntStateOf(0) }

    var selectedDate by remember { mutableLongStateOf(0L) }
    var selectedEndTime: TimePickerState? by remember { mutableStateOf(null) }
    var selectedStartTime: TimePickerState? by remember { mutableStateOf(null) }

    var prevSizeStrength by remember { mutableIntStateOf(strengthWorkoutState.value.listOfExercise.size) }
    var prevSizeCardio by remember { mutableIntStateOf(cardioWorkoutState.value.listOfExercise.size) }
    var prevSizeHit by remember { mutableIntStateOf(cardioWorkoutState.value.listOfExercise.size) }

    LaunchedEffect(strengthWorkoutState.value.listOfExercise.size) {
        if (strengthWorkoutState.value.listOfExercise.size > prevSizeStrength) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSizeStrength = strengthWorkoutState.value.listOfExercise.size
    }

    LaunchedEffect(cardioWorkoutState.value.listOfExercise.size) {
        if (cardioWorkoutState.value.listOfExercise.size > prevSizeCardio) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSizeCardio = cardioWorkoutState.value.listOfExercise.size
    }

    LaunchedEffect(hitWorkoutState.value.listOfExercise.size) {
        if (hitWorkoutState.value.listOfExercise.size > prevSizeHit) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSizeHit = hitWorkoutState.value.listOfExercise.size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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

            if (statusWorkout != "") {
                Text(
                    text = "Choose type of your workout: ",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 17.sp)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TypeOfWorkout.forEach { workout ->
                        FilterChip(
                            selected = typeOfWorkout == workout,
                            onClick = {
                                typeOfWorkout = workout
                            },
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
            }
            if (typeOfWorkout.isNotEmpty()) {

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
        }

        if (typeOfWorkout == "Strength training") {

            strengthWorkoutState.value.listOfExercise.forEachIndexed { index, exercise ->
                //key(exercise.id) {

                var localTempName by remember { mutableStateOf("Click to change the name") }

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
                                color = Color.White,
                                fontSize = 12.sp
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                workoutViewModel.editNameForStrengthExercise(
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
                                focusedContainerColor = Color(0xFF18120B),
                                unfocusedContainerColor = Color(0xFF18120B),
                                focusedIndicatorColor = Color(0xFF18120B),
                                unfocusedIndicatorColor = Color(0xFF18120B),
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .offset(x = (-15).dp)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        workoutViewModel.editNameForStrengthExercise(
                                            localTempName,
                                            exercise.id
                                        )
                                    } else {
                                        localTempName = ""
                                    }
                                })

                        IconButton(onClick = {
                            workoutViewModel.deleteStrengthExercise(exercise.id)
                            strengthExerciseLimit--
                            Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                .show()

                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                        //}
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
                                workoutViewModel.updateKgInSet(exercise.id, set.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(color = Color.White),
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
                                workoutViewModel.updateRepsInSet(exercise.id, set.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = "reps.", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteSetFromExercise(set.id, exercise.id)
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
                        if (strengthWorkoutState.value.listOfExercise.lastIndex == index) {
                            coroutineScope.launch {
                                delay(100)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        workoutViewModel.addSetToExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a set")
                }
            }

            if (selectedDate != 0L && selectedEndTime != null && selectedStartTime != null ||
                (statusWorkout == "In Progress" && selectedEndTime != null && selectedStartTime != null)
            ) {

                Button(
                    onClick = {
                        workoutViewModel.addExerciseForStrengthWorkout("")
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
                    val updatedWorkout = strengthWorkoutState.value.copy(
                        liftedKg = strengthWorkoutState.value.listOfExercise.sumOf { exercise ->
                            exercise.listOfSets.sumOf { set ->
                                set.reps.toInt() * set.kg.toInt()
                            }
                        },
                        type = "Strength Workout",
                        exerciseCount = strengthWorkoutState.value.listOfExercise.size,
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
                        reminderWorkoutState = updatedWorkout
                        showBottomSheet = true
                    } else {
                        workoutViewModel.saveWorkoutToDb(
                            workoutState = updatedWorkout,
                            onSuccess = {
                                showBottomSheet = false
                                navigateToMyWorkoutsScreen()
                                Toast.makeText(
                                    context,
                                    "Workout successfully added.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            })
                    }
                },
                    shape = RoundedCornerShape(4.dp),
                    enabled =
                    strengthWorkoutState.value.listOfExercise.all { it.listOfSets.isNotEmpty() }
                            && strengthWorkoutState.value.listOfExercise.isNotEmpty()
                            && strengthWorkoutState.value.listOfExercise.all { it.listOfSets.all { it.kg != "" } }
                            && strengthWorkoutState.value.listOfExercise.all { it.listOfSets.all { it.reps != "" } },
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

        if (typeOfWorkout == "Cardio session") {

            cardioWorkoutState.value.listOfExercise.forEachIndexed { index, exercise ->
                //key(exercise.id) {

                var localTempName by remember { mutableStateOf("Click to change the name") }

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
                                color = Color.White, fontSize = 12.sp
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                workoutViewModel.editNameForCardioExercise(
                                    localTempName, exercise.id
                                )
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF18120B),
                                unfocusedContainerColor = Color(0xFF18120B),
                                focusedIndicatorColor = Color(0xFF18120B),
                                unfocusedIndicatorColor = Color(0xFF18120B),
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .offset(x = (-15).dp)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        workoutViewModel.editNameForCardioExercise(
                                            localTempName,
                                            exercise.id
                                        )
                                    } else {
                                        localTempName = ""
                                    }
                                })

                        IconButton(onClick = {
                            workoutViewModel.deleteCardioExercise(exercise.id)
                            cardioExerciseLimit--
                            Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                        //}
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
                                workoutViewModel.updateKcalInCardio(exercise.id, cardio.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White, textAlign = TextAlign.Center
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
                                workoutViewModel.updateDurationInCardio(exercise.id, cardio.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = ".", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteCardioFromExercise(cardio.id, exercise.id)
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
                        workoutViewModel.addCardioToExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a cardio")
                }
            }
            if (selectedDate != 0L && selectedEndTime != null && selectedStartTime != null ||
                (statusWorkout == "In Progress" && selectedEndTime != null && selectedStartTime != null)
            ) {

                Button(
                    onClick = {
                        workoutViewModel.addExerciseForCardioWorkout("")
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
                    val updatedWorkout = cardioWorkoutState.value.copy(
                        type = "Cardio Workout",
                        caloriesBurned = cardioWorkoutState.value.listOfExercise.sumOf { exercise ->
                            exercise.listOfCardio.sumOf { it.kcal.toIntOrNull() ?: 0 }
                        },
                        exerciseCount = cardioWorkoutState.value.listOfExercise.size,
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
                        reminderWorkoutState = updatedWorkout
                        showBottomSheet = true
                    } else {
                        workoutViewModel.saveWorkoutToDb(
                            workoutState = updatedWorkout,
                            onSuccess = {
                                showBottomSheet = false
                                navigateToMyWorkoutsScreen()
                                Toast.makeText(
                                    context,
                                    "Workout successfully added.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            })
                    }

                },
                    shape = RoundedCornerShape(4.dp),
                    enabled =
                    cardioWorkoutState.value.listOfExercise.all { it.listOfCardio.isNotEmpty() }
                            && cardioWorkoutState.value.listOfExercise.isNotEmpty()
                            && cardioWorkoutState.value.listOfExercise.all { it.listOfCardio.all { it.kcal != "" } }
                            && cardioWorkoutState.value.listOfExercise.all { it.listOfCardio.all { it.duration != "" } },
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

        if (typeOfWorkout == "HIT training") {

            hitWorkoutState.value.listOfExercise.forEachIndexed { index, exercise ->
                //key(exercise.id) {

                var localTempName by remember { mutableStateOf("Click to change the name") }

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
                                color = Color.White, fontSize = 12.sp
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                workoutViewModel.editNameForHitExercise(
                                    localTempName, exercise.id
                                )
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF18120B),
                                unfocusedContainerColor = Color(0xFF18120B),
                                focusedIndicatorColor = Color(0xFF18120B),
                                unfocusedIndicatorColor = Color(0xFF18120B),
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .offset(x = (-15).dp)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) {
                                        workoutViewModel.editNameForHitExercise(
                                            localTempName,
                                            exercise.id
                                        )
                                    } else {
                                        localTempName = ""
                                    }
                                })

                        IconButton(onClick = {
                            workoutViewModel.deleteHitExercise(exercise.id)
                            hitExerciseLimit--
                            Toast.makeText(context, "Exercise deleted", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                        //}
                    }
                }

                exercise.listOfHits.forEachIndexed { _, hit ->
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
                                workoutViewModel.updateDurationInHit(exercise.id, hit.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White, textAlign = TextAlign.Center
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
                                workoutViewModel.updateRestInHit(exercise.id, hit.id, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = "sec.", style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = {
                            workoutViewModel.deleteHitFromExercise(hit.id, exercise.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete, contentDescription = null
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (hitWorkoutState.value.listOfExercise.lastIndex == index) {
                            coroutineScope.launch {
                                delay(100)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        workoutViewModel.addHitToExercise(exercise.id)
                    },
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Text("Add a HIT")
                }
            }
            if (selectedDate != 0L && selectedEndTime != null && selectedStartTime != null ||
                statusWorkout == "In Progress" && selectedEndTime != null && selectedStartTime != null
            ) {

                Button(
                    onClick = {
                        workoutViewModel.addExerciseForHitWorkout("")
                        hitExerciseLimit++
                    },
                    shape = RoundedCornerShape(4.dp),
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
                    val updatedWorkout = hitWorkoutState.value.copy(
                        type = "HIT Workout",
                        hitsLasted = hitWorkoutState.value.listOfExercise.sumOf { exercise ->
                            exercise.listOfHits.sumOf { it.duration.toIntOrNull() ?: 0 }
                        },
                        exerciseCount = hitWorkoutState.value.listOfExercise.size,
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
                        reminderWorkoutState = updatedWorkout
                        showBottomSheet = true
                    } else {
                        workoutViewModel.saveWorkoutToDb(
                            workoutState = updatedWorkout,
                            onSuccess = {
                                showBottomSheet = false
                                navigateToMyWorkoutsScreen()
                                Toast.makeText(
                                    context,
                                    "Workout successfully added.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            })
                    }

                },
                    shape = RoundedCornerShape(4.dp),
                    enabled = hitWorkoutState.value.listOfExercise.all { it.listOfHits.isNotEmpty() }
                            && hitWorkoutState.value.listOfExercise.isNotEmpty()
                            && hitWorkoutState.value.listOfExercise.all { it.listOfHits.all { it.duration != "" } }
                            && hitWorkoutState.value.listOfExercise.all { it.listOfHits.all { it.rest != "" } },
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
                                "Workout successfully added.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        })
                }
            },
            onSetNotificationClick = {

                val tempListOfHours = mutableListOf<Long>()
                val hourOptions = listOf(1, 2, 4, 8, 12, 24)

                checkedStates.forEachIndexed { index, checked ->
                    if (checked) {
                        tempListOfHours.add(hourOptions[index] * 60 * 60 * 1000L)
                    }
                }

                // Obliczamy dokładny czas treningu, traktując workoutStart jako offset
                val workoutTimestamp = calculateWorkoutTimestamp(
                    reminderWorkoutState.workoutDate,
                    reminderWorkoutState.workoutStart
                )

                val tempListOfDates = mutableListOf<Timestamp>()
                tempListOfHours.forEach { offset ->
                    val reminderMillis = workoutTimestamp.toDate().time - offset
                    tempListOfDates.add(Timestamp(Date(reminderMillis)))
                }

                val futureReminders = tempListOfDates.filter { it >= Timestamp.now() }

                workoutViewModel.addToDbReminder(
                    listOfDates = futureReminders,
                    listOfHours = tempListOfHours,
                    userFcmToken = userInformationState.value.fcmToken,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Workout successfully added with reminders.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    workoutState = reminderWorkoutState,
                    workoutId = reminderWorkoutState.id,
                )

                showBottomSheet = false
                navigateToMyWorkoutsScreen()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal2(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    finishedWorkout: Boolean,
) {
    val today = LocalDate.now()
    val currentTimeMillis = System.currentTimeMillis()

    val selectableDates: SelectableDates = if (finishedWorkout) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                return !date.isAfter(today)
            }
        }
    } else {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                return !date.isBefore(today)  // Tylko dzisiejsza i przyszłe daty
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentTimeMillis,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


fun getWorkoutStatusText(status: String): String {
    return when (status) {
        "Finished" -> "Please specify the dates and times at which you started and finished your workout."
        "Planned" -> "Select the date and time for your upcoming workout."
        else -> "Select the start time of your workout and its planned end time."
    }
}

fun dateConverter(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDate: LocalDateTime? = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.getDefault())
    return if (localDate != null) {
        localDate.format(formatter)
    } else {
        ""
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
fun timePickerStateToString(timePickerState: TimePickerState): String {
    return String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelection(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
    startTime: TimePickerState?,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    val isTimeValid = startTime?.let {
        val startTotalMinutes = it.hour * 60 + it.minute
        val selectedTotalMinutes = timePickerState.hour * 60 + timePickerState.minute
        selectedTotalMinutes >= startTotalMinutes
    } ?: true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isTimeValid) {
                    Text(
                        text = "End time must be after start time.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timePickerState) },
                shape = RoundedCornerShape(4.dp),
                enabled = isTimeValid
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

//trwanie treningu w milisekundach
@OptIn(ExperimentalMaterial3Api::class)
fun calculateWorkoutDuration(startTime: TimePickerState, endTime: TimePickerState): Long {
    val startMinutes = startTime.hour * 60 + startTime.minute
    val endMinutes = endTime.hour * 60 + endTime.minute
    return (endMinutes - startMinutes).toLong() * 60000L
}

@OptIn(ExperimentalMaterial3Api::class)
fun timePickerToLong(time: TimePickerState): Long {
    return (time.hour * 60 + time.minute) * 60000L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetNotification(
    stateList: SnapshotStateList<Boolean>,
    sheetState: SheetState,
    sheetStateChange: (Boolean) -> Unit,
    onNoClick: () -> Unit,
    onSetNotificationClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { sheetStateChange(false) }, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
        {
            Text(
                "Do you want to receive a Notification prior to your workout?",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 20.sp)
            )

            Spacer(Modifier.height(10.dp))

            workoutReminderOptions.chunked(2).forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEachIndexed { colIndex, item ->
                            val index = rowIndex * 2 + colIndex

                            Checkbox(
                                checked = stateList[index],
                                onCheckedChange = { newValue -> stateList[index] = newValue }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = item, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSetNotificationClick() },
                    enabled = stateList.any { it }, // if even one element is false (not selected) than false
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Set Notification")
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = {
                        onNoClick()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = stateList.all { !it },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("No")
                }
            }
        }
    }
}

fun calculateWorkoutTimestamp(workoutDateMillis: Long, workoutStartMillis: Long): Timestamp {
    val zone = ZoneId.systemDefault()

    // Konwertujemy workoutDateMillis na LocalDate (wyodrębniając tylko datę)
    val localDate = Instant.ofEpochMilli(workoutDateMillis)
        .atZone(zone)
        .toLocalDate()

    // workoutStartMillis traktujemy jako przesunięcie od północy
    val localTime = LocalTime.MIDNIGHT.plus(Duration.ofMillis(workoutStartMillis))

    // Łączymy datę i czas w LocalDateTime
    val localDateTime = LocalDateTime.of(localDate, localTime)

    // Konwertujemy LocalDateTime na Instant
    val instant = localDateTime.atZone(zone).toInstant()

    // Tworzymy obiekt Timestamp wykorzystując Date.
    return Timestamp(Date(instant.toEpochMilli()))
}


