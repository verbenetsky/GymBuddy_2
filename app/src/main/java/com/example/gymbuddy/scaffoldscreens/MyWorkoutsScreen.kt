package com.example.gymbuddy.scaffoldscreens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.chat.ChatViewModel
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.datasource.SortAndFilterOption.additionalSortingOptionCardioWorkout
import com.example.gymbuddy.datasource.SortAndFilterOption.additionalSortingOptionHITWorkout
import com.example.gymbuddy.datasource.SortAndFilterOption.additionalSortingOptionStrengthWorkout
import com.example.gymbuddy.datasource.SortAndFilterOption.typeOptions
import com.example.gymbuddy.datasource.SortAndFilterOption.sortingOptions
import com.example.gymbuddy.datasource.SortAndFilterOption.statusOptions
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.utils.CommonUtils.workoutStateToMarkdown
import com.example.gymbuddy.workout.WorkoutState
import com.example.gymbuddy.workout.WorkoutViewModel
import com.example.gymbuddy.workout.dateConverter
import com.google.firebase.firestore.Query
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyWorkoutsScreen(
    innerNavController: NavController,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    navigateToAddWorkoutScreen: () -> Unit,
    navigateToEditScreen: () -> Unit,
    workoutViewModel: WorkoutViewModel = hiltViewModel(
        innerNavController.getBackStackEntry("about_screen")
    ),
) {
    val context = LocalContext.current
    var selectedWorkoutForDeletion by remember { mutableStateOf<WorkoutState?>(null) }
    var dialogState by remember { mutableStateOf(false) }
    val workoutListState = workoutViewModel.listOfWorkouts.collectAsState()
    val workoutsState =
        workoutViewModel.workoutsState.collectAsState() // stan calej listy workouts (Loading or Loaded)

    var workoutToShare by remember { mutableStateOf(WorkoutState()) }

    val sheetStateShareWithFriend = rememberModalBottomSheetState()
    var showBottomSheetShareWithFriend by remember { mutableStateOf(false) }

    val sheetStateSortFilter = rememberModalBottomSheetState()
    var showBottomSheetSortFilter by remember { mutableStateOf(false) }

    val friendsList =
        friendRequestViewModel.friendList.collectAsState()

    var selectedOption by remember { mutableStateOf(sortingOptions[0]) }

    val checkedStatesType =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(typeOptions.size) { false }) } }

    val checkedStatesStatus =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(statusOptions.size) { false }) } }

    var mapOfFilters by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    var sortingOption by remember { mutableStateOf("workoutDate") }
    var sortingDirection by remember { mutableStateOf(Query.Direction.ASCENDING) }

    val tempFriendIdToFindAChannel by remember { mutableStateOf("") }

    // od old wiecej milisekund minelo
    when (selectedOption) {
        "Date from old to new" -> {
            sortingOption = "workoutDate"
            sortingDirection = Query.Direction.ASCENDING
        }

        "Date from new to old" -> {
            sortingOption = "workoutDate"
            sortingDirection = Query.Direction.DESCENDING
        }

        "Amount of exercise descending" -> {
            sortingOption = "exerciseCount"
            sortingDirection = Query.Direction.DESCENDING
        }

        "Amount of exercise ascending" -> {
            sortingOption = "exerciseCount"
            sortingDirection = Query.Direction.ASCENDING
        }

        "Duration ascending" -> {
            sortingOption = "workoutTime"
            sortingDirection = Query.Direction.ASCENDING
        }

        "Duration descending" -> {
            sortingOption = "workoutTime"
            sortingDirection = Query.Direction.DESCENDING
        }

        "Calories burned ascending" -> {
            sortingOption = "caloriesBurned"
            sortingDirection = Query.Direction.ASCENDING
        }

        "Calories burned descending" -> {
            sortingOption = "caloriesBurned"
            sortingDirection = Query.Direction.DESCENDING
        }

        "Overall lifted descending" -> {
            sortingOption = "liftedKg"
            sortingDirection = Query.Direction.DESCENDING
        }

        "Overall lifted ascending" -> {
            sortingOption = "liftedKg"
            sortingDirection = Query.Direction.ASCENDING
        }

        "HITs lasted ascending" -> {
            sortingOption = "hitsLasted"
            sortingDirection = Query.Direction.ASCENDING
        }

        "HITs lasted descending" -> {
            sortingOption = "hitsLasted"
            sortingDirection = Query.Direction.DESCENDING
        }
    }

    mapOfFilters = buildMap {
        val statuses = mutableListOf<String>()
        if (checkedStatesStatus[0]) statuses.add("Finished")
        if (checkedStatesStatus[1]) statuses.add("Planned")
        if (checkedStatesStatus[2]) statuses.add("In Progress")
        if (statuses.isNotEmpty()) put("status", statuses)

        val types = mutableListOf<String>()
        if (checkedStatesType[0]) types.add("Strength Workout")
        if (checkedStatesType[1]) types.add("HIT Workout")
        if (checkedStatesType[2]) types.add("Cardio Workout")
        if (types.isNotEmpty()) put("type", types)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddWorkoutScreen() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Workout"
                )
            }
        },
        content = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(60.dp)
                            .padding(8.dp)
                            .clickable {
                                showBottomSheetSortFilter = true
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Sort & Filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Filter"
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter"
                            )
                        }
                    }
                }
                if (workoutsState.value == WorkoutViewModel.WorkoutsState.Loading) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (workoutsState.value == WorkoutViewModel.WorkoutsState.Loaded) {
                    items(workoutListState.value) { workout ->
                        Workout(
                            onShareClick = {
                                if (friendsList.value.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "You don't have any friends to share with.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    workoutToShare = workout
                                    showBottomSheetShareWithFriend = true
                                }
                            },
                            onCardClick = {
                                workoutViewModel.tryToEditWorkout(workout)
                                navigateToEditScreen()
                            },
                            onEditClick = {
                                workoutViewModel.tryToEditWorkout(workout)
                                navigateToEditScreen()
                            },
                            workoutState = workout,
                            onDeleteClick = {
                                dialogState = true
                                selectedWorkoutForDeletion = workout
                            }
                        )
                    }
                } else if (workoutsState.value == WorkoutViewModel.WorkoutsState.Loaded && workoutListState.value.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nothing found",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 25.sp)
                            )
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nothing found",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 25.sp)
                            )
                        }
                    }
                }
            }

            if (dialogState) {
                selectedWorkoutForDeletion?.let { it1 ->
                    AlertDialogDeleteWorkout(
                        changeDialogState = { newValue -> dialogState = newValue },
                        onRemoveWorkoutClick = {
                            println(it1.id)
                            workoutViewModel.removeWorkoutFromDb(it1.id) {
                                Toast.makeText(
                                    context,
                                    "Workout deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        workoutState = it1
                    )
                }
            }
            if (showBottomSheetShareWithFriend) {
                ModalBottomSheetSendWorkoutToFriend(
                    sheetState = sheetStateShareWithFriend,
                    sheetStateChange = { newValue -> showBottomSheetShareWithFriend = newValue },
                    friendsList = friendsList.value,
                    onShareWorkoutWithFriendClick = { friendInfo ->

                        chatViewModel.findChannelIdUsingUsersId(
                            friendInfo.userId,
                            onSuccess = { channelId ->
                                if (channelId != null) {
                                    chatViewModel.sendMessage(
                                        channelID = channelId,
                                        messageText = workoutStateToMarkdown(workoutToShare),
                                        image = null,
                                        receiverFcmToken = friendInfo.fcmToken,
                                        isShareWorkoutMessage = true,
                                        senderUsername = ""
                                    )
                                    Toast.makeText(
                                        context,
                                        "Workout Send",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Try again later",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }, onFailure = {
                                Toast.makeText(
                                    context,
                                    "Something gone terribly wrong! Try again",
                                    Toast.LENGTH_LONG
                                ).show()
                            })
                        showBottomSheetShareWithFriend = false

                    },
                )
            }

            if (showBottomSheetSortFilter) {
                ModalBottomSheetSortFilter(
                    sheetState = sheetStateSortFilter,
                    typeList = checkedStatesType,
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                    statusList = checkedStatesStatus,
                    onSaveClick = {
                        workoutViewModel.listenForWorkouts(
                            sortField = sortingOption,
                            direction = sortingDirection,
                            filters = mapOfFilters
                        )
                        showBottomSheetSortFilter = false

                    },
                    sheetStateChange = { newValue -> showBottomSheetSortFilter = newValue },
                )
            }
        }
    )
}

@Composable
fun Workout( // singel record of workout
    onShareClick: () -> Unit,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    workoutState: WorkoutState,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(150.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = workoutState.type,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dateConverter(workoutState.workoutDate),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onDeleteClick() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete icon")
                }

                IconButton(onClick = { onEditClick() }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "edit icon")
                }
                IconButton(onClick = { onShareClick() }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "share icon")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${workoutState.status}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = if (workoutState.listOfExercise.size <= 1) "${workoutState.listOfExercise.size} " +
                            "exercise" else "${workoutState.listOfExercise.size} exercises",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${formatDuration(workoutState.workoutEnd - workoutState.workoutStart)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
                val message = when (workoutState.type) {
                    "Cardio Workout" -> {
                        val totalCalories = workoutState.listOfExercise.sumOf { exercise ->
                            exercise.listOfCardio.sumOf { it.kcal.toIntOrNull() ?: 0 }
                        }
                        val prefix = if (workoutState.status == "Planned")
                            "Your cardio will burn:"
                        else
                            "Your cardio burned:"
                        "$prefix $totalCalories kcal"
                    }

                    "HIT Workout" -> {
                        val totalDuration = workoutState.listOfExercise.sumOf { exercise ->
                            exercise.listOfHits.sumOf { it.duration.toIntOrNull() ?: 0 }
                        }
                        val prefix = if (workoutState.status == "Planned")
                            "Overall your hits will last:"
                        else
                            "Overall your hits lasted:"
                        "$prefix $totalDuration sec"
                    }

                    "Strength Workout" -> {
                        val totalWeight = workoutState.listOfExercise.sumOf { exercise ->
                            exercise.listOfSets.sumOf { set ->
                                set.reps.toInt() * set.kg.toInt()
                            }
                        }
                        val prefix = if (workoutState.status == "Planned")
                            "Overall you will lift:"
                        else
                            "Overall you lifted:"
                        "$prefix $totalWeight kg"
                    }

                    else -> ""
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
}

@Composable
fun AlertDialogDeleteWorkout(
    changeDialogState: (Boolean) -> Unit,
    onRemoveWorkoutClick: (WorkoutState) -> Unit,
    workoutState: WorkoutState,
) {
    AlertDialog(
        onDismissRequest = {
            changeDialogState(false)
        },
        title = {
            Text(text = "Remove Workout")
        },
        text = {
            Text("Are you sure you want to remove this workout?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRemoveWorkoutClick(workoutState)
                    changeDialogState(false)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    changeDialogState(false)
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetSendWorkoutToFriend(
    sheetState: SheetState,
    friendsList: List<UserFoundInformation>,
    onShareWorkoutWithFriendClick: (UserFoundInformation) -> Unit,
    sheetStateChange: (Boolean) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { sheetStateChange(false) }, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
        {
            Text(
                "Pick a friend to share your workout with:",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 20.sp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(friendsList) { friend ->
                    SingleRecordOfFriendsLists(
                        userFoundInformation = friend,
                        onRecordClick = {
                            onShareWorkoutWithFriendClick(friend)
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// filter:
// Strength Workout \
// HIT Workout       | -> jesli jedno z tych to otwieraja sie nowe opcje sortingu
// Cardio Workout   /

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetSortFilter(
    sheetState: SheetState,
    selectedOption: String,
    typeList: SnapshotStateList<Boolean>,
    statusList: SnapshotStateList<Boolean>,
    sheetStateChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { sheetStateChange(false) }, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sort & Filter",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 25.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            var groups by remember { mutableStateOf(listOf(sortingOptions)) }

            groups = buildList {
                add(sortingOptions)
                if (typeList.getOrNull(0) == true) { // Strength Workout
                    add(additionalSortingOptionStrengthWorkout)
                }
                if (typeList.getOrNull(1) == true) { // HIT Workout
                    add(additionalSortingOptionHITWorkout)
                }
                if (typeList.getOrNull(2) == true) { // Cardio Workout
                    add(additionalSortingOptionCardioWorkout)
                }
            }

            Column(modifier = Modifier.selectableGroup()) {
                groups.flatten().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            onClick = null,
                            selected = (option == selectedOption),
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Text(text = option)

                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Column {
                typeOptions.forEachIndexed { index, item -> // 1-Strength Workout
                    Row(                                    // 2-HIT Workout
                        modifier = Modifier                 // 3-Cardio Workout
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = typeList[index],
                            onCheckedChange = { newValue -> typeList[index] = newValue }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))


            Column {
                statusOptions.forEachIndexed { index, item ->   // 1-Finished
                    // 2-Planned
                    // 3-In Progress
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = statusList[index],
                            onCheckedChange = { newValue -> statusList[index] = newValue }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Save")
            }
        }
    }
}



