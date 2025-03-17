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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.gymbuddy.datasource.StatusOfWorkoutData.statusOfWorkout
import com.example.gymbuddy.datasource.TypeOfWorkoutData.TypeOfWorkout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(workoutViewModel: WorkoutViewModel = hiltViewModel()) {
    val context = LocalContext.current

    var statusWorkout by remember { mutableStateOf("") }
    var typeOfWorkout by remember { mutableStateOf("") }

    var strengthExerciseLimit by remember { mutableIntStateOf(0) }
    var cardioExerciseLimit by remember { mutableIntStateOf(0) }
    var hitExerciseLimit by remember { mutableIntStateOf(0) }

    var selectedDate by remember { mutableLongStateOf(12L) } //  delete

    val currentTime = Calendar.getInstance() // delete

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY), // delete
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    var selectedEndTime: TimePickerState? by remember { mutableStateOf(timePickerState) } // delete

    var selectedStartTime: TimePickerState? by remember { mutableStateOf(timePickerState) } // delete

    var showDatePicker by remember { mutableStateOf(false) }

    var showStartDial by remember { mutableStateOf(false) }
    var showEndDial by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val strengthWorkoutState = workoutViewModel.strengthWorkoutState.collectAsStateWithLifecycle()
    val cardioWorkoutState = workoutViewModel.cardioWorkoutState.collectAsStateWithLifecycle()

    var prevSize by remember { mutableIntStateOf(strengthWorkoutState.value.listOfExercise.size) }

    LaunchedEffect(strengthWorkoutState.value.listOfExercise.size) {
        if (strengthWorkoutState.value.listOfExercise.size > prevSize) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSize = strengthWorkoutState.value.listOfExercise.size
    }

    LaunchedEffect(cardioWorkoutState.value.listOfExercise.size) {
        if (cardioWorkoutState.value.listOfExercise.size > prevSize) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        prevSize = strengthWorkoutState.value.listOfExercise.size
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
                            onClick = { typeOfWorkout = workout },
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

//                Spacer(modifier = Modifier.height(4.dp))
//
//                Text(
//                    text = "You can choose your own type of workout",
//                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 17.sp)
//                )
//
//                OutlinedTextField(
//                    value = typeOfWorkout,
//                    onValueChange = { typeOfWorkout = it },
//                    placeholder = { Text(text = "Type here") },
//                    colors = OutlinedTextFieldDefaults.colors(),
//                    modifier = Modifier
//                        .padding(4.dp)
//                        .wrapContentSize()
//                        .fillMaxWidth()
//                )
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
                        }) {
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
                            textStyle = MaterialTheme.typography.titleSmall.copy(color = Color.White),
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
                                focusedContainerColor = Color(0xFF000000),
                                unfocusedContainerColor = Color(0xFF000000),
                                focusedIndicatorColor = Color(0xFF000000),
                                unfocusedIndicatorColor = Color(0xFF000000),
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .offset(x = (-25).dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        localTempName = ""
                                    }
                                })

                        IconButton(onClick = {
                            workoutViewModel.deleteStrengthExercise(exercise.id)
                            strengthExerciseLimit--
                            Toast.makeText(context, "Exercised deleted", Toast.LENGTH_SHORT)
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

            if (selectedDate != 0L && selectedEndTime != null && selectedStartTime != null) {

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

                },
                    shape = RoundedCornerShape(4.dp),
                    enabled = strengthWorkoutState.value.listOfExercise.all { it.listOfSets.isNotEmpty() }
                            && strengthWorkoutState.value.listOfExercise.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)

                ) {
                    Text(
                        text = "Save the Workout",
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
                            textStyle = MaterialTheme.typography.titleSmall.copy(color = Color.White),
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
                                focusedContainerColor = Color(0xFF000000),
                                unfocusedContainerColor = Color(0xFF000000),
                                focusedIndicatorColor = Color(0xFF000000),
                                unfocusedIndicatorColor = Color(0xFF000000),
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .offset(x = (-25).dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
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
            if (selectedDate != 0L && selectedEndTime != null && selectedStartTime != null) {

                Button(
                    onClick = {
                        workoutViewModel.addExerciseForCardioWorkout("")
                        cardioExerciseLimit ++
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

                },
                    shape = RoundedCornerShape(4.dp),
                    enabled = strengthWorkoutState.value.listOfExercise.all { it.listOfSets.isNotEmpty() }
                            && strengthWorkoutState.value.listOfExercise.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)

                ) {
                    Text(
                        text = "Save the Workout",
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
            onDismiss = { showDatePicker = false }
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
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal2(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val currentTimeMillis = System.currentTimeMillis()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentTimeMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                return !date.isBefore(today)
            }
        }
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


private fun getWorkoutStatusText(status: String): String {
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
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Time")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timePickerState) },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text("Cancel")
            }
        }
    )
}

