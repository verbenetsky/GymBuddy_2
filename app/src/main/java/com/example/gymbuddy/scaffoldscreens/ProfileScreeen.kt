package com.example.gymbuddy.scaffoldscreens

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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserInformation
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.res.stringResource
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.datasource.SportsData.sports
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.utils.CommonUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

@Composable
fun ProfileScreen(
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    authState: SignInViewModel.AuthState,
    userManagementViewModel: UserManagementViewModel,
    modifier: Modifier = Modifier
) {
    val userInformationState by userManagementViewModel.userInformationState.collectAsState()
    var enableEdit by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChipsDialog by remember { mutableStateOf(false) }
    var selectedHobbies by remember { mutableStateOf<List<String>>(emptyList()) }
    var showImageDialog by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

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
    if (imageUri != null) {
        LaunchedEffect(imageUri) {
            println("this")
            userManagementViewModel.addProfilePictureUrlToViewModel(imageUri!!)
        }
    }

    val dashLength = 50f
    val gapLength = 50f

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dashLength + gapLength,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    if (authState == SignInViewModel.AuthState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column {
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
                            { showDeleteDialog = true },
                            { newValue -> enableEdit = newValue },
                            enableEdit
                        )
                    }


                    Image(
                        painter = rememberAsyncImagePainter(userInformationState.profilePictureUrl),
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
                                        color = Color.White,
                                        size = size,
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = dashEffect
                                        ),
                                        cornerRadius = CornerRadius(0.dp.toPx(), 0.dp.toPx())
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

                    Row(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(horizontal = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = userInformationState.firstName,
                            onValueChange = { userManagementViewModel.updateFirstName(it) },
                            enabled = enableEdit,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.appBarTitle.copy(
                                fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.7,
                                textAlign = TextAlign.Center,
                            ),
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = Color.White,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 2.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = Color.White,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(5.dp.toPx(), 100.dp.toPx())
                                        )
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = userInformationState.lastName,
                            onValueChange = { userManagementViewModel.updateLastName(it) },
                            enabled = enableEdit,
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = Color.White,

                                ),
                            textStyle = MaterialTheme.typography.appBarTitle.copy(
                                fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.7,
                                textAlign = TextAlign.Center,

                                ),
                            modifier = Modifier
                                .weight(1f)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 2.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = Color.White,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(5.dp.toPx(), 100.dp.toPx())
                                        )
                                    }
                                }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Row(modifier = Modifier
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 1.5.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = Color.White,
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
                                }) {
                                Text(
                                    text = "            @",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                                )
                                BasicTextField(
                                    value = userInformationState.username,
                                    onValueChange = { userManagementViewModel.updateUsername(it) },
                                    enabled = enableEdit,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(color = Color.White),
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
                                disabledTextColor = Color.White, disabledLabelColor = Color.White,
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                )
                            },
                            label = { Text("Email") })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DateOfBirthManagement(
                        userInformationState = userInformationState,
                        onDateSelected = { date ->
                            userManagementViewModel.updateDateOfBirth(date!!)
                            showDatePicker = false
                        },
                        onShowDatePickerChange = { newValue -> showDatePicker = newValue },
                        enableEdit = enableEdit,
                        showDatePicker = showDatePicker,
                        onDismiss = { showDatePicker = false },
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                            .drawBehind {
                                if (enableEdit) {
                                    val strokeWidth = 2.dp.toPx()

                                    val dashEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(dashLength, gapLength),
                                        phase = phase
                                    )
                                    drawRoundRect(
                                        color = Color.White,
                                        size = size,
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = dashEffect
                                        ),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 100.dp.toPx())
                                    )
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {

                        TextField(
                            modifier = Modifier
                                .offset(y = 1.dp)
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 2.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = Color.White,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 100.dp.toPx())
                                        )
                                    }
                                },
                            value = userInformationState.hobbies.joinToString(", "),
                            readOnly = true,
                            singleLine = true,
                            onValueChange = {},
                            maxLines = 2,
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = Color.White, disabledLabelColor = Color.White,
                                disabledIndicatorColor = Color(0xFF462A00),
                                focusedIndicatorColor = Color(0xFF462A00),
                                unfocusedIndicatorColor = Color(0xFF462A00),
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
                            label = { Text("Hobbies:") })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {

                        TextField(
                            modifier = modifier
                                .fillMaxWidth()
                                .offset(y = 1.dp)
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                                .drawBehind {
                                    if (enableEdit) {
                                        val strokeWidth = 2.dp.toPx()

                                        val dashEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(dashLength, gapLength),
                                            phase = phase
                                        )
                                        drawRoundRect(
                                            color = Color.White,
                                            size = size,
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = dashEffect
                                            ),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 100.dp.toPx())
                                        )
                                    }

                                },
                            value = userInformationState.goal,
                            readOnly = !enableEdit,
                            singleLine = true,
                            onValueChange = { userManagementViewModel.updateGoal(it) },
                            maxLines = 2,
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = Color.White, disabledLabelColor = Color.White,
                                disabledIndicatorColor = Color(0xFF462A00),
                                focusedIndicatorColor = Color(0xFF462A00),
                                unfocusedIndicatorColor = Color(0xFF462A00),
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Email Icon"
                                )
                            },

                            label = { Text("Goal:") })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!checkSaveCondition(userInformationState)) {
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
                enableEdit = enableEdit && checkSaveCondition(userInformationState),
                onSaveClick = {
                    onSaveClick()
                    enableEdit = false
                })

            AlertDialogDeleteAccount(
                dialogState = showDeleteDialog,
                changeDialogState = { newValue -> showDeleteDialog = newValue },
                onDeleteClick = { onDeleteClick() }
            )

            AlertDialogSelectChips(
                showChipsDialog = showChipsDialog,
                onConfirmButtonClickChipsSelect = { selectedList ->
                    selectedHobbies = selectedList
                    userManagementViewModel.updateHobbies(selectedList)
                },
                onChangeDialogState = { newValue -> showChipsDialog = newValue },
            )

            AlertDialogChangeImage(
                showImageDialog = showImageDialog,
                onConfirmButtonClickChangeImage = {
                    openDocumentLauncher.launch(arrayOf("image/*"))
                },
                onChangeDialogState = { newValue -> showImageDialog = newValue }
            )
        }
    }

}

@Composable
fun ActionButtons(
    onDeleteClick: () -> Unit,
    onEnableEditChange: (Boolean) -> Unit,
    enableEdit: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row {
        IconButton(onClick = { onEnableEditChange(!enableEdit) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Account"
            )
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete account") },
                onClick = { onDeleteClick() }
            )
        }
    }
}


@Composable
private fun formatDate(date: Long): String {
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
    userInformationState: UserInformation,
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
            .padding(start = 4.dp, end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "    Date of birth:",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .offset(y = (3).dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatDate(userInformationState.dateOfBirth),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .offset(y = (3).dp)
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
                shape = RoundedCornerShape(4.dp)
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
    onConfirmButtonClickChipsSelect: (List<String>) -> Unit,
    onChangeDialogState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showChipsDialog) {
        val selectedSports = remember { mutableStateListOf<String>() }

        val listSize = selectedSports.size
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
                            selected = selectedSports.contains(sport),
                            onClick = {
                                if (selectedSports.contains(sport)) {
                                    selectedSports.remove(sport)
                                } else {
                                    selectedSports.add(sport)
                                }
                            },
                            enabled = selectedSports.contains(sport) || listSize < 5,
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
                            text = stringResource(R.string.selected_x_out_of_5, listSize),
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
                        onConfirmButtonClickChipsSelect(selectedSports)
                    }, enabled = listSize == 5
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
                Text("You sure you want to change your image?")
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
            }
        )
    }
}


private fun checkSaveCondition(userInformationState: UserInformation): Boolean {
    return userInformationState.firstName.isNotEmpty() &&
            userInformationState.lastName.isNotEmpty() &&
            userInformationState.username.isNotEmpty() &&
            userInformationState.goal.isNotEmpty() &&
            userInformationState.hobbies.size >= 5
}


