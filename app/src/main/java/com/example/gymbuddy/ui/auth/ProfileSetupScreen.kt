package com.example.gymbuddy.ui.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymbuddy.R
import com.example.gymbuddy.ui.profile.UserInformation
import com.example.gymbuddy.ui.profile.UserManagementViewModel
import com.example.gymbuddy.data.source.SportsData.sports
import com.example.gymbuddy.ui.common.rememberImeState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_SIZE_LINE = 15

@Composable
fun ProfileSetupScreen(
    userManagementViewModel: UserManagementViewModel = hiltViewModel(),
    authViewModel: AuthViewModel,
    navigateToMyApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userInformationState by userManagementViewModel.userInformationState.collectAsState()
    val userDate by authViewModel.userData.collectAsState()
    val context = LocalContext.current

    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    val selectedSports = remember { mutableStateListOf<String>() }
    val listSize = selectedSports.size

    LaunchedEffect(userInformationState) {
        println(userInformationState)
    }

    LaunchedEffect(imeState.value) {
        if (imeState.value) {
            delay(25)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(Unit) {
        userManagementViewModel.transportUserInformation(userDate) // dodajemy do UserInfo
        println(authState)
    }

    if (authState == AuthViewModel.AuthState.Loading) {
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
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp)
        ) {
            RegistrationTitle()
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            HobbiesSection(selectedSports, listSize)
            Spacer(modifier = Modifier.height(8.dp))
            NameInputFields(userManagementViewModel, userInformationState)
            Spacer(modifier = Modifier.height(8.dp))
            UsernameAndGoalFields(userManagementViewModel, userInformationState)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            BirthdaySection(userInformationState) { showDatePicker = it }
            Spacer(modifier = Modifier.height(8.dp))
            ContinueButton(
                onContinueClick = {
                    // najpierw dodajemy hobby do viewModelu
                    userManagementViewModel.addHobbies(selectedSports.toList())

                    //potem sprawdzamy czy taki username jest wolny
                    userManagementViewModel.addUsernameToDataBase(
                        username = userInformationState.username,
                        // jesli username jest wolny
                        onSuccess = {
                            userManagementViewModel.addUser(
                                userInformation = userInformationState,
                                onError = { e ->
                                    println(e)
                                    Toast.makeText(
                                        context,
                                        "Error: $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Information successfully added",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) // dodajemy takiego Usera do bazy danych

                            authViewModel.updateAuthState(AuthViewModel.AuthState.Authenticated) // ustawiamy stan na Authenticated
                            userManagementViewModel.fetchUserData(false)  // dodajemy fcm token do bazy danych
                            authViewModel.clearUserData() // czyszcimy userData
                            navigateToMyApp() // przechodzimy do aplikacji
                        },
                        // jesli nie wolny username
                        onFailure = {
                            userManagementViewModel.updateUsername("")
                            Toast.makeText(
                                context,
                                "Username is already taken, try another one",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                isEnabled = isContinueEnabled(userInformationState, selectedSports)
            )
        }
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { date ->
                    userManagementViewModel.updateDateOfBirth(date!!)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun RegistrationTitle() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text = stringResource(R.string.registration),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun HobbiesSection(
    selectedSports: SnapshotStateList<String>,
    listSize: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.whats_your_hobbies),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        FilterChips(selectedSports, listSize)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChips(
    selectedSports: SnapshotStateList<String>,
    listSize: Int
) {

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
                    if (selectedSports.contains(sport)) selectedSports.remove(sport)
                    else selectedSports.add(sport)
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
fun NameInputFields(
    userInformationViewModel: UserManagementViewModel,
    userInformationState: UserInformation,
) {
    val onlyLettersRegex = Regex("^[a-zA-Z]*$")

    Row(modifier = Modifier.fillMaxWidth()) {
        //firstName
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
        //lastName
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
fun UsernameAndGoalFields(
    userInformationViewModel: UserManagementViewModel,
    userInformationState: UserInformation
) {

    Row(modifier = Modifier.fillMaxWidth()) {
        //userName
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
        // Goal
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
    userInformationState: UserInformation,
    onShowDatePickerChange: (Boolean) -> Unit
) {

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
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }, enabled = datePickerState.selectedDateMillis != null
            ) {
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
        shape = RoundedCornerShape(10.dp),
        enabled = isEnabled
    ) {
        Text(text = stringResource(R.string.continue_text))
    }
}

@Composable
private fun isContinueEnabled(
    userInformationState: UserInformation,
    selectedSports: SnapshotStateList<String>
): Boolean {

    return userInformationState.firstName.isNotEmpty() &&
            userInformationState.lastName.isNotEmpty() &&
            userInformationState.username.isNotEmpty() &&
            userInformationState.goal.isNotEmpty() &&
            userInformationState.dateOfBirth != 0L &&
            selectedSports.size == 5
}
