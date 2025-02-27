package com.example.gymbuddy.scaffoldscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.buttonState.ButtonStateManager
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SearchScreen(
    userSearchState: UserSearchViewModel.UserSearchState,
    onSearchClick: (UserFoundInformation) -> Unit,
    onGuestProfileClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDeclineClick: () -> Unit,
    buttonStateManager: ButtonStateManager,
    onSendRequestClick: () -> Unit,
    userSearchViewModel: UserSearchViewModel,
    modifier: Modifier = Modifier
) {
    val userSearchQuery by userSearchViewModel.searchQuery.collectAsState()
    val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState()
    var alertDialogState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userSearchViewModel.clearSearchQuery()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 16.dp)
    ) {
        OutlinedTextField(
            value = userSearchQuery,
            onValueChange = { userSearchViewModel.updateUserSearchQuery(it) },
            singleLine = true,
            label = { Text(text = "Enter user's username to search") },
            trailingIcon = {
                IconButton(onClick = { onSearchClick(userFoundInformation) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (userSearchState == UserSearchViewModel.UserSearchState.FoundUser) {
            SingleRecordOfSearch(
                onProfileClick = onProfileClick,
                onSendRequestClick = onSendRequestClick,
                userSearchViewModel = userSearchViewModel,
                buttonStateManager = buttonStateManager,
                onGuestProfileClick = onGuestProfileClick,
                onDeclineClick = onDeclineClick,
                onRemoveClick = { alertDialogState = true }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (userSearchState) {
                    UserSearchViewModel.UserSearchState.NothingFound ->
                        Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)

                    UserSearchViewModel.UserSearchState.Loading ->
                        CircularProgressIndicator()

                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        AlertDialogDeleteFriend(
            dialogState = alertDialogState,
            changeDialogState = { newState -> alertDialogState = newState },
            onRemoveFriendClick = { onRemoveClick() },
            userFoundInformation = userFoundInformation
        )
    }
}


@Composable
fun SingleRecordOfSearch(
    onProfileClick: () -> Unit,
    onSendRequestClick: () -> Unit,
    onGuestProfileClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDeclineClick: () -> Unit,
    buttonStateManager: ButtonStateManager,
    userSearchViewModel: UserSearchViewModel,
    modifier: Modifier = Modifier
) {
    val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState()
    val buttonStatus by buttonStateManager.buttonState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    Card(
        modifier = modifier
            .fillMaxWidth()
            .size(125.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            var painter = painterResource(id = R.drawable.default_profile_picture)

            if (userFoundInformation.profilePictureUrl.isNotEmpty()) {
                painter = rememberAsyncImagePainter(userFoundInformation.profilePictureUrl)
            }
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(125.dp)
                    .aspectRatio(1f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Text(
                        text = userFoundInformation.firstName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.9f)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = userFoundInformation.lastName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.9f)
                    )
                }
                Row {
                    Text(
                        text = "@",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * 1.1)
                    )
                    Text(
                        text = userFoundInformation.username,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * 1.1)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (userFoundInformation.userId != userId) {
                        Button(
                            onClick = {
                                if (buttonStatus == "Remove")
                                    onRemoveClick()
                                else if (buttonStatus == "Decline")
                                    onDeclineClick()
                                else
                                    onSendRequestClick()
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f),
                            enabled = buttonStatus == "Send Request" || buttonStatus == "Send Message" || buttonStatus == "Remove" || buttonStatus == "Decline",
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                when (buttonStatus) {
                                    "Send Request" -> Color(0x0F0D791C).copy(alpha = 0.7f)
                                    "Remove" -> Color(0xFFD31212)
                                    "Decline" -> Color(0xFFD31212)
                                    else -> Color(0xFFFFCA89)
                                }
                            )
                        ) {
                            Text(
                                text = buttonStatus,
                                fontSize = 13.sp,
                                color = when (buttonStatus) {
                                    "Send Request" -> Color.White
                                    "Remove" -> Color.White
                                    "Decline" -> Color.White
                                    else -> LocalContentColor.current
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            if (userFoundInformation.userId == userId) {
                                onProfileClick()
                            } else
                                onGuestProfileClick()
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFF855305).copy(alpha = 0.7f)
                        )
                    ) {
                        Text(text = "See Profile", fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
