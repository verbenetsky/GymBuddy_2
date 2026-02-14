package com.example.gymbuddy.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import com.example.gymbuddy.ui.theme.appBarTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.stringResource
import com.example.gymbuddy.ui.auth.AuthViewModel
import com.example.gymbuddy.data.source.SportsData.sports
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.ui.messages.ChannelViewModel
import com.example.gymbuddy.ui.chatbot.ChatBotViewModel
import com.example.gymbuddy.ui.messages.ChatViewModel
import com.example.gymbuddy.ui.friends.FriendRequestViewModel
import com.example.gymbuddy.ui.theme.surfaceDark
import com.example.gymbuddy.ui.workout.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ProfileScreen(
    userManagementViewModel: UserManagementViewModel,
    signInViewModel: AuthViewModel,
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
    chatBotViewModel: ChatBotViewModel = hiltViewModel(),
    channelViewModel: ChannelViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val userInformationState by userManagementViewModel.userInformationState.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    var enableEdit by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChipsDialog by remember { mutableStateOf(false) }
    var expandedMoreOptions by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imageState by userManagementViewModel.imageState.collectAsState()

    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    //-------------------------------------EditTemp-------------------------------------------------
    var tempFirstName by remember { mutableStateOf(userInformationState.firstName) }
    var tempLastName by remember { mutableStateOf(userInformationState.lastName) }
    var tempUsername by remember { mutableStateOf(userInformationState.username) }
    var tempDateOfBirth by remember { mutableLongStateOf(userInformationState.dateOfBirth) }
    var tempGoal by remember { mutableStateOf(userInformationState.goal) }
    var tempListOfHobbies by remember { mutableStateOf(userInformationState.hobbies) }
    //----------------------------------------------------------------------------------------------

    // otwieranie gallery ze zdjeciami
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        println("profile picture url:")
        println(userInformationState.profilePictureUrl)
    }

    // jesli zdjecie profilowe ktore wybralismy z gallery nie jest null to odrazu updejtujemy w bazie danych a potem w viewModelu
    if (imageUri != null) {
        LaunchedEffect(imageUri) {
            userManagementViewModel.uploadProfilePicture(imageUri!!, userInformationState.userId)
        }
    }

    val dashLength = 400f
    val gapLength = 150f

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dashLength + gapLength,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (!isDarkTheme) MaterialTheme.colorScheme.surface else surfaceDark)
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        ActionButtons(
                            onEnableEditChange = { newValue -> enableEdit = newValue },
                            enableEdit = enableEdit,
                            onExpandedChange = { newValue ->
                                expandedMoreOptions = newValue
                            },
                            expandedMoreOptions = expandedMoreOptions,
                            onDeleteClick = {
                                showDeleteDialog = true
                            }
                        )
                    }

                    Box {
                        Image(
                            painter = if (userInformationState.profilePictureUrl != "") rememberAsyncImagePainter(
                                userInformationState.profilePictureUrl
                            ) else painterResource(id = R.drawable.default_profile_picture),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(150.dp)
                                .offset(y = (-15).dp)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 2.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = if (isDarkTheme) Color.White else Color.Black,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(
                                                0.dp.toPx(),
                                                0.dp.toPx()
                                            )
                                        )
                                    }
                                }
                                .padding(6.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (enableEdit) {
                                        showImageDialog = true
                                    }
                                },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                        if (imageState == UserManagementViewModel.ImageState.LoadingImage) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            //firstName
                            BasicTextField(
                                value = tempFirstName,
                                onValueChange = { tempFirstName = it },
                                enabled = enableEdit,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.appBarTitle.copy(
                                    fontSize = MaterialTheme.typography.appBarTitle.fontSize,
                                    textAlign = TextAlign.Center,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 0.dp)
                                    .drawBehind {
                                        if (enableEdit) {
                                            val strokeWidth = 1.dp.toPx()
                                            val dashEffect = PathEffect.dashPathEffect(
                                                intervals = floatArrayOf(
                                                    dashLength,
                                                    gapLength
                                                ),
                                                phase = phase
                                            )
                                            drawRoundRect(
                                                color = if (isDarkTheme) Color.White else Color.Black,
                                                size = size,
                                                style = Stroke(
                                                    width = strokeWidth,
                                                    pathEffect = dashEffect
                                                ),
                                                cornerRadius = CornerRadius(
                                                    5.dp.toPx(),
                                                    100.dp.toPx()
                                                )
                                            )
                                        }
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // lastname
                            BasicTextField(
                                value = tempLastName,
                                onValueChange = { tempLastName = it },
                                enabled = enableEdit,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.appBarTitle.copy(
                                    fontSize = MaterialTheme.typography.appBarTitle.fontSize,
                                    textAlign = TextAlign.Center,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 0.dp)
                                    .drawBehind {
                                        if (enableEdit) {
                                            val strokeWidth = 1.dp.toPx()
                                            val dashEffect = PathEffect.dashPathEffect(
                                                intervals = floatArrayOf(
                                                    dashLength,
                                                    gapLength
                                                ),
                                                phase = phase
                                            )
                                            drawRoundRect(
                                                color = if (isDarkTheme) Color.White else Color.Black,
                                                size = size,
                                                style = Stroke(
                                                    width = strokeWidth,
                                                    pathEffect = dashEffect
                                                ),
                                                cornerRadius = CornerRadius(
                                                    5.dp.toPx(),
                                                    100.dp.toPx()
                                                )
                                            )
                                        }
                                    }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .wrapContentWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .drawBehind {
                                        if (enableEdit) {
                                            val strokeWidth = 1.dp.toPx()
                                            val dashEffect = PathEffect.dashPathEffect(
                                                intervals = floatArrayOf(
                                                    dashLength,
                                                    gapLength
                                                ),
                                                phase = phase
                                            )
                                            drawRoundRect(
                                                color = if (isDarkTheme) Color.White else Color.Black,
                                                size = size,
                                                style = Stroke(
                                                    width = strokeWidth,
                                                    pathEffect = dashEffect
                                                ),
                                                cornerRadius = CornerRadius(
                                                    5.dp.toPx(),
                                                    100.dp.toPx()
                                                )
                                            )
                                        }
                                    }
                            ) {
                                Text(
                                    text = "@ ",
                                    style = MaterialTheme.typography.titleMedium.copy(color = if (isDarkTheme) Color.White else Color.Black)
                                )
                                BasicTextField(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .wrapContentHeight(),
                                    value = tempUsername,
                                    onValueChange = {
                                        if (it.matches(usernamePattern.toRegex())) {
                                            tempUsername = it
                                        }
                                    },
                                    enabled = enableEdit,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isDarkTheme) Color.White else Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .fillMaxWidth()
                    ) {
                        TextField(
                            modifier = Modifier
                                .offset(y = 1.dp)
                                .padding(start = 8.dp),
                            value = userInformationState.email,
                            enabled = false,
                            singleLine = true,
                            onValueChange = { },
                            textStyle = MaterialTheme.typography.titleMedium,

                            colors = TextFieldDefaults.colors(
                                disabledTextColor = LocalContentColor.current,
                                disabledLabelColor = LocalContentColor.current,
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                )
                            },
                            label = {
                                Text(
                                    "Email",
                                )
                            })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DateOfBirthManagement(
                        onDateSelected = { date ->
                            tempDateOfBirth = date ?: 0L
                            showDatePicker = false
                        },
                        onShowDatePickerChange = { newValue -> showDatePicker = newValue },
                        enableEdit = enableEdit,
                        showDatePicker = showDatePicker,
                        onDismiss = { showDatePicker = false },
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, bottom = 4.dp)
                            .drawBehind {
                                if (enableEdit) {
                                    val strokeWidth = 1.dp.toPx()
                                    val dashEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(dashLength, gapLength),
                                        phase = phase
                                    )
                                    drawRoundRect(
                                        color = if (isDarkTheme) Color.White else Color.Black,
                                        size = size,
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = dashEffect
                                        ),
                                        cornerRadius = CornerRadius(
                                            5.dp.toPx(),
                                            100.dp.toPx()
                                        )
                                    )
                                }
                            },
                        tempDate = tempDateOfBirth,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        // hobbies
                        TextField(
                            modifier = Modifier
                                .offset(y = 1.dp)
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 1.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = if (isDarkTheme) Color.White else Color.Black,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(
                                                5.dp.toPx(),
                                                100.dp.toPx()
                                            )
                                        )
                                    }
                                },
                            value = tempListOfHobbies.joinToString(", "),
                            readOnly = true,
                            singleLine = true,
                            onValueChange = {},
                            maxLines = 2,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),

                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Email Icon"
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showChipsDialog = true },
                                    enabled = enableEdit
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Icon"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    "Hobbies:"
                                )
                            })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        // goal
                        TextField(
                            modifier = modifier
                                .fillMaxWidth()
                                .offset(y = 1.dp)
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 1.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = if (isDarkTheme) Color.White else Color.Black,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(
                                                5.dp.toPx(),
                                                100.dp.toPx()
                                            )
                                        )
                                    }

                                },
                            value = tempGoal,
                            readOnly = !enableEdit,
                            singleLine = true,
                            onValueChange = { tempGoal = it },
                            maxLines = 2,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Email Icon"
                                )
                            },
                            label = {
                                Text(
                                    "Goal:",
                                )
                            })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!checkSaveCondition(
                            lastName = tempLastName,
                            username = tempUsername,
                            goal = tempGoal,
                            firstName = tempFirstName,
                        )
                    ) {
                        Text(
                            text = "All field should be filled",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            SaveButtonSection(
                enableEdit = enableEdit && checkSaveCondition(
                    lastName = tempLastName,
                    username = tempUsername,
                    goal = tempGoal,
                    firstName = tempFirstName,
                ),
                onSaveClick = {
                    userManagementViewModel.updateUser(
                        newUserData = UserInformation(
                            firstName = tempFirstName,
                            lastName = tempLastName,
                            dateOfBirth = tempDateOfBirth,
                            hobbies = tempListOfHobbies,
                            goal = tempGoal,
                        ),
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Information successfully updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            userManagementViewModel.fetchUserData()
                        },
                        onFailure = {
                            Toast.makeText(
                                context,
                                "An Error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailureAddUsername = {
                            Toast.makeText(
                                context,
                                "Username already taken",
                                Toast.LENGTH_LONG
                            ).show()
                            tempUsername = userInformationState.username
                        },
                        oldUsername = userInformationState.username,
                        newUsername = tempUsername
                    )
                    enableEdit = false
                })

            AlertDialogDeleteAccount(
                dialogState = showDeleteDialog,
                changeDialogState = { newValue -> showDeleteDialog = newValue },
                onDeleteClick = {
                    isLoading = true
                    scope.launch {
                        chatViewModel.deleteAllMessagesRelatedToUser() // usuwamy wszystkie messagers ktore znajduja sie w tyhc czatach gdzie jest nasz user
                        chatBotViewModel.deleteConversation() // usuwany czat z botem - +++
                        workoutViewModel.removeAllWorkoutsFromDb() // usuwamy wszystkie treningi - +++
                        channelViewModel.deleteAllChannels() // usuwamy wszsktie konwersacje tego usera - +++
                        friendRequestViewModel.deleteAllFriendRequest() // usuwamy wszsytkie friend requests - +++
                        userManagementViewModel.deleteProfilePictureFromStorage(userInformationState.profilePictureUrl) // usuwamy zdjecie profilowe
                        userManagementViewModel.deleteUserDataFromFirestore( // usuwamy najpierw z bazy danych usera - +++ (rowniez usuwamy jego username  - +++)
                            onSuccess = {
                                onDeleteClick() // nawigacja do sing_in
                                scope.launch {
                                    signInViewModel.deleteUserAccount( // potem usuwamy z firebase Auth
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Account successfully deleted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            signInViewModel.setAuthState(AuthViewModel.AuthState.Unauthenticated)
                                            // ustawiamy na niezalogowany
                                        },
                                        onError = {
                                            Toast.makeText(
                                                context,
                                                "An error occurred while deleting an account",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Something gone wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            userId = userInformationState.userId,
                            username = tempUsername
                        )
                    }
                    expandedMoreOptions = false
                }
            )

            AlertDialogSelectChips(
                showChipsDialog = showChipsDialog,
                onSaveButton = { outputListOfHobbies ->
                    tempListOfHobbies = outputListOfHobbies
                },
                onChangeDialogState = { newValue -> showChipsDialog = newValue },
                tempListOfHobbies = tempListOfHobbies,
            )

            AlertDialogChangeImage(
                showImageDialog = showImageDialog,
                onConfirmButtonClickChangeImage = {
                    openDocumentLauncher.launch(arrayOf("image/*"))
                    scope.launch {
                        userManagementViewModel.deleteProfilePictureFromStorage(userInformationState.profilePictureUrl) // usuwamy obecne zdjecie profilowe z storage
                        userManagementViewModel.deleteProfilePictureFromFirestore() // w fireStore rowniez zerujemy pole profilePictureUri
                        userManagementViewModel.updateProfilePictureToDefault() // dodajemy defualtowe zdjecie odrazu po usunieciu
                    }
                },
                onChangeDialogState = { newValue -> showImageDialog = newValue }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(if (isSystemInDarkTheme()) Color(0xFF18120B) else Color(0xFFFFF8F4)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun ActionButtons(
    onEnableEditChange: (Boolean) -> Unit,
    enableEdit: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    expandedMoreOptions: Boolean,
    onDeleteClick: () -> Unit
) {
    Row {
        IconButton(onClick = { onEnableEditChange(!enableEdit) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Account"
            )
        }
        IconButton(onClick = { onExpandedChange(!expandedMoreOptions) }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expandedMoreOptions,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Delete account") },
                onClick = { onDeleteClick() }
            )
        }
    }
}


@Composable
fun formatDate(date: Long): String {
    val formattedDate = remember(date) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
            Date(date)
        )
    }
    return formattedDate
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthManagement(
    tempDate: Long,
    onDateSelected: (Long?) -> Unit,
    onShowDatePickerChange: (Boolean) -> Unit,
    enableEdit: Boolean,
    showDatePicker: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Date of birth:",
                modifier = Modifier
                    .padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatDate(tempDate),
                modifier = Modifier
                    .padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onShowDatePickerChange(!showDatePicker) },
                enabled = enableEdit,
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Date Icon",
                )
            }
        }

        if (showDatePicker) {
            val today = System.currentTimeMillis()
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = today,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= today
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
    }
}

@Composable
fun SaveButtonSection(
    enableEdit: Boolean,
    onSaveClick: () -> Unit
) {
    AnimatedVisibility(
        visible = enableEdit,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight / 2 }
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight / 2 }
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        ) {
            Button(
                onClick = { onSaveClick() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.displayMedium,
                )
            }
        }
    }
}

@Composable
fun AlertDialogDeleteAccount(
    dialogState: Boolean,
    changeDialogState: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    if (dialogState) {
        AlertDialog(
            onDismissRequest = {
                changeDialogState(false)
            },
            title = {
                Text(text = "Delete Account")
            },
            text = {
                Text("You sure you want to delete your account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertDialogSelectChips(
    showChipsDialog: Boolean,
    tempListOfHobbies: List<String>,
    onSaveButton: (List<String>) -> Unit,
    onChangeDialogState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showChipsDialog) {
        var tempListOfHobbies2 by remember { mutableStateOf(tempListOfHobbies) } // to co potem przekazemy do Save
        AlertDialog(
            onDismissRequest = { onChangeDialogState(false) },
            title = {
                Text(text = "Change Hobbies")
            },
            text = {
                FlowRow(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    maxItemsInEachRow = 5,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy((-4).dp),
                    overflow = FlowRowOverflow.Clip
                ) {
                    sports.forEach { sport ->
                        FilterChip(
                            selected = tempListOfHobbies2.contains(sport),
                            onClick = {
                                if (tempListOfHobbies2.contains(sport)) {
                                    tempListOfHobbies2 = tempListOfHobbies2 - sport
                                } else {
                                    tempListOfHobbies2 = tempListOfHobbies2 + sport
                                }
                            },
                            enabled = tempListOfHobbies2.contains(sport) || tempListOfHobbies2.size < 5,
                            label = {
                                Text(
                                    sport,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            modifier = Modifier
                                .height(40.dp)
                                .padding(vertical = 6.dp)
                        )
                    }
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.selected_x_out_of_5,
                                tempListOfHobbies2.size
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChangeDialogState(false)
                        onSaveButton(tempListOfHobbies2)
                    }, enabled = tempListOfHobbies2.size == 5
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onChangeDialogState(false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AlertDialogChangeImage(
    showImageDialog: Boolean,
    onConfirmButtonClickChangeImage: () -> Unit,
    onChangeDialogState: (Boolean) -> Unit,
) {
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = {
                onChangeDialogState(false)
            },
            title = {
                Text(text = "Change Image")
            },
            text = {
                Text("You sure you want to change your image?\nYour old image will be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmButtonClickChangeImage()
                        onChangeDialogState(false)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onChangeDialogState(false)
                    }
                ) {
                    Text("Cancel")
                }
            })
    }
}

private fun checkSaveCondition(
    firstName: String,
    lastName: String,
    username: String,
    goal: String,
): Boolean {
    return firstName.isNotEmpty() &&
            lastName.isNotEmpty() &&
            username.isNotEmpty() &&
            goal.isNotEmpty()
}

private val usernamePattern: Pattern = Pattern.compile("^[a-zA-Z0-9_.-]*$")



