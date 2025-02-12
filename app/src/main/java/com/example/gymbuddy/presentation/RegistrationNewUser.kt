package com.example.gymbuddy.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.datasource.SportsData.sports
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_SIZE_LINE = 15

@Composable
fun RegistrationScreen(
    userInformationViewModel: UserManagementViewModel,
    viewModel: SignInViewModel,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            RegistrationTitle()
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            HobbiesSection(userInformationViewModel)
            Spacer(modifier = Modifier.height(8.dp))
            NameInputFields(userInformationViewModel)
            Spacer(modifier = Modifier.height(8.dp))
            UsernameAndGoalFields(userInformationViewModel)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            BirthdaySection(userInformationViewModel) { showDatePicker = it }
            Spacer(modifier = Modifier.height(8.dp))
            ContinueButton(
                onContinueClick = onContinueClick,
                isEnabled = isContinueEnabled(userInformationViewModel)
            )
        }

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { date ->
                    userInformationViewModel.updateDateOfBirth(date!!)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}


@Composable
fun RegistrationTitle() {
    Text(
        text = stringResource(R.string.registration),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HobbiesSection(userInformationViewModel: UserManagementViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.whats_your_hobbies),
            style = MaterialTheme.typography.displayLarge
        )
        FilterChips(userInformationViewModel)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChips(userInformationViewModel: UserManagementViewModel) {
    val selectedSports = remember { mutableStateListOf<String>() }
    val listSize = selectedSports.size

    FlowRow(
        modifier = Modifier
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
                        userInformationViewModel.removeHobby(sport)
                    } else {
                        selectedSports.add(sport)
                        userInformationViewModel.addHobby(sport)
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
}

@Composable
fun NameInputFields(userInformationViewModel: UserManagementViewModel) {
    val userInformationState by userInformationViewModel.userInformationState.collectAsState()
    val onlyLettersRegex = Regex("^[a-zA-Z]*$")

    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = userInformationState.firstName,
            singleLine = true,
            onValueChange = { newValue ->
                if (newValue.matches(onlyLettersRegex) && newValue.length <= 15) {
                    userInformationViewModel.updateFirstName(newValue)
                }
            },
            label = { Text("First Name", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = userInformationState.lastName,
            singleLine = true,
            onValueChange = { newValue ->
                if (newValue.matches(onlyLettersRegex) && newValue.length <= MAX_SIZE_LINE) {
                    userInformationViewModel.updateLastName(newValue)
                }
            },
            label = { Text("Last Name", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun UsernameAndGoalFields(userInformationViewModel: UserManagementViewModel) {
    val userInformationState by userInformationViewModel.userInformationState.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = userInformationState.username,
            singleLine = true,
            onValueChange = {
                if (it.length <= MAX_SIZE_LINE)
                    userInformationViewModel.updateUsername(it)
            },
            label = { Text("User Name", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = userInformationState.goal,
            singleLine = true,
            onValueChange = {
                userInformationViewModel.updateGoal(it)
            },
            label = { Text("What's your goal?", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BirthdaySection(
    userInformationViewModel: UserManagementViewModel,
    onShowDatePickerChange: (Boolean) -> Unit
) {
    val userInformationState by userInformationViewModel.userInformationState.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.when_is_your_birthday),
            style = MaterialTheme.typography.displayLarge,
        )

        IconButton(onClick = { onShowDatePickerChange(true) }) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Date Icon",
            )
        }

        if (userInformationState.dateOfBirth != 0L) {
            val formattedDate = remember(userInformationState.dateOfBirth) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    Date(userInformationState.dateOfBirth)
                )
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .offset(y = (-2).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
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

@Composable
fun ContinueButton(
    onContinueClick: () -> Unit,
    isEnabled: Boolean
) {
    Button(
        onClick = onContinueClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        enabled = isEnabled
    ) {
        Text(text = stringResource(R.string.continue_text))
    }
}

@Composable
private fun isContinueEnabled(userInformationViewModel: UserManagementViewModel): Boolean {
    val userInformationState by userInformationViewModel.userInformationState.collectAsState()

    return userInformationState.firstName.isNotEmpty() &&
            userInformationState.lastName.isNotEmpty() &&
            userInformationState.username.isNotEmpty() &&
            userInformationState.goal.isNotEmpty() &&
            userInformationState.dateOfBirth != 0L &&
            userInformationState.hobbies.size == 5
}
