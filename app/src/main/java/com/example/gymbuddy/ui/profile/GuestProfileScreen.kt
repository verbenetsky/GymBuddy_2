package com.example.gymbuddy.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.ui.messages.ChannelViewModel
import com.example.gymbuddy.ui.messages.ChatViewModel
import com.example.gymbuddy.data.model.UserFoundInformation
import com.example.gymbuddy.ui.auth.AuthViewModel
import com.example.gymbuddy.ui.search.UserSearchViewModel
import com.example.gymbuddy.data.repositoryImpl.FriendRequestRepositoryImpl
import com.example.gymbuddy.data.model.FriendRequestInformationDto
import com.example.gymbuddy.data.model.SendingRequestStatus
import com.example.gymbuddy.data.model.AcceptOrDeclineOrRemoveFriendDto
import com.example.gymbuddy.ui.friends.FriendRequestViewModel
import com.example.gymbuddy.ui.friends.AlertDialogDeleteFriend
import com.example.gymbuddy.ui.theme.appBarTitle
import com.example.gymbuddy.ui.theme.surfaceDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun GuestProfileScreen(
    authState: AuthViewModel.AuthState,
    userSearchViewModel: UserSearchViewModel,
    friendRequestViewModel: FriendRequestViewModel,
    userManagementViewModel: UserManagementViewModel,
    chatViewModel: ChatViewModel = hiltViewModel(),
    channelViewModel: ChannelViewModel = hiltViewModel(),
    innerNavController: NavController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val uid = Firebase.auth.currentUser?.uid ?: return
    val context = LocalContext.current
    val userFoundInformationState by userSearchViewModel.userFoundInformation.collectAsState()
    val currentUserInformation by userManagementViewModel.userInformationState.collectAsState()

    var buttonStateExpanded by remember { mutableStateOf("") }
    var alertDialogState by remember { mutableStateOf(false) }

    val darkTheme: Boolean = isSystemInDarkTheme()

    DisposableEffect(uid, userFoundInformationState.userId) {
        friendRequestViewModel.startObservingButton(uid, userFoundInformationState.userId)
        onDispose { friendRequestViewModel.stopObservingButton() }
    }

    // Teraz możesz bezpiecznie kolekcjonować stan przycisku
    val buttonState by friendRequestViewModel.buttonState
        .map { it?.name ?: "" }
        .collectAsState(initial = FriendRequestRepositoryImpl.FriendButtonState.SendRequest.name)

    when (buttonState) {
        FriendRequestRepositoryImpl.FriendButtonState.SendRequest.toString() -> {
            buttonStateExpanded = "Send Friend Request"
        } // nie pokazywac send message

        FriendRequestRepositoryImpl.FriendButtonState.RequestSent.toString() -> {
            buttonStateExpanded = "Request has been sent"
        } // nie pokazywac send message

        FriendRequestRepositoryImpl.FriendButtonState.Remove.toString() -> {
            buttonStateExpanded = "Remove"
        } // pokazywac Send Message

        FriendRequestRepositoryImpl.FriendButtonState.Decline.toString() -> {
            buttonStateExpanded = "Decline"
        } // nie pokazywac send message
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
            modifier = Modifier
                .fillMaxSize()
                .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
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
                    Spacer(modifier = Modifier.height(32.dp))

                    Box {
                        Image(
                            painter = if (userFoundInformationState.profilePictureUrl != "") rememberAsyncImagePainter(
                                userFoundInformationState.profilePictureUrl
                            ) else painterResource(id = R.drawable.default_profile_picture),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(150.dp)
                                .offset(y = (-15).dp)
                                .padding(6.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    }
                    Text(
                        "${userFoundInformationState.firstName} ${userFoundInformationState.lastName}",
                        style = MaterialTheme.typography.appBarTitle.copy(color = if (darkTheme) Color.White else Color.Black),
                        fontSize = MaterialTheme.typography.appBarTitle.fontSize,
                    )
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
                                    .width(IntrinsicSize.Min),
                            ) {
                                Text(
                                    text = "@",
                                    style = MaterialTheme.typography.titleMedium.copy(color = if (darkTheme) Color.White else Color.Black)
                                )
                                BasicTextField(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .wrapContentHeight(),
                                    value = userFoundInformationState.username,
                                    onValueChange = {},
                                    enabled = false,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(color = if (darkTheme) Color.White else Color.Black)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp)
                    ) {
                        if (buttonStateExpanded == "Decline") {
                            Button(
                                onClick = {
                                    friendRequestViewModel.acceptFriendRequest(
                                        currentUserId = Firebase.auth.currentUser?.uid ?: "",
                                        friendId = userFoundInformationState.userId,
                                        onSuccess = {
                                            friendRequestViewModel.sendAcceptNotification(
                                                AcceptOrDeclineOrRemoveFriendDto(
                                                    currentUserInformation.username,
                                                    userFoundInformationState.fcmToken
                                                )
                                            )
                                            Toast.makeText(
                                                context,
                                                "Friends request accepted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    Color(0xFF198D29)
                                )
                            ) {
                                Text(
                                    text = "Accept",
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {

                                when (buttonStateExpanded) {
                                    "Remove" -> {
                                        alertDialogState = true
                                    }

                                    "Send Friend Request" -> {
                                        friendRequestViewModel.sendFriendRequest(
                                            friendRequestDto = FriendRequestInformationDto(
                                                receiverId = userFoundInformationState.userId,
                                                senderId = Firebase.auth.currentUser?.uid ?: "",
                                                date = System.currentTimeMillis(),
                                                status = SendingRequestStatus.PENDING
                                            ),
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "Friend request sent",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onFailure = {

                                            }
                                        )
                                        buttonStateExpanded = "Request has been sent"
                                    }

                                    "Decline" -> {
                                        friendRequestViewModel.declineFriendRequest(
                                            currentUserId = Firebase.auth.currentUser?.uid ?: "",
                                            friendId = userFoundInformationState.userId,
                                            onSuccess = {
                                                friendRequestViewModel.sendDeclineNotification(
                                                    AcceptOrDeclineOrRemoveFriendDto(
                                                        receiverFcmToken = userFoundInformationState.fcmToken,
                                                        senderName = currentUserInformation.username
                                                    )
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Friend request declined",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onFailure = {
                                                println("error while declining friend request")
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(if (buttonStateExpanded != "Decline") 2f else 1f),
                            enabled = buttonStateExpanded != "Request has been sent",
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                if (buttonStateExpanded == "Remove" || buttonStateExpanded == "Decline")
                                    Color(0xFFD31212).copy(alpha = 0.7f)
                                else
                                    Color(0xFF198D29)
                            )
                        ) {
                            Text(
                                text = buttonStateExpanded,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        if (buttonStateExpanded == "Remove") {

                            Button(
                                onClick = {
                                    scope.launch {
                                        // 1) próbuj znaleźć kanał
                                        val existingChannelId = channelViewModel.findChannel(userFoundInformationState.userId)

                                        println("error is :")
                                        Log.e("ERROR", "existingChannelId is: $existingChannelId")

                                        // 2) jeśli pusty, to utwórz nowy i przypisz jego ID
                                        val channelId = existingChannelId.ifEmpty {
                                            channelViewModel.addChannelSuspend(
                                                userFoundInformationState.userId
                                            )
                                        }

                                        // 3) pobierz suspend-owo profil usera
                                        val userInfo = userSearchViewModel.getUserBasedOnUserId(userFoundInformationState.userId)
                                        val fullName = "${userInfo?.firstName} ${userInfo?.lastName}"



                                        // 4) ustaw tytuł czatu
                                        chatViewModel.updateCurrentChatName(fullName)

                                        Log.e("ERROR", "chatsss/$channelId/${userFoundInformationState.userId}")

                                        // 5) nawiguj
                                        innerNavController.navigate("chat/$channelId")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(
                                    text = "Send Message",
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
                            value = userFoundInformationState.email,
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

                    DateOfBirthInformationGuess(
                        userFoundInformationState = userFoundInformationState,
                        isDarkTheme = darkTheme,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
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
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                            value = userFoundInformationState.hobbies.joinToString(", "),
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
                            label = {
                                Text(
                                    "Hobbies:",
                                )
                            })
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
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                            value = userFoundInformationState.goal,
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
                }
            }
        }
    }
    AlertDialogDeleteFriend(
        dialogState = alertDialogState,
        changeDialogState = { newValue -> alertDialogState = newValue },
        onRemoveFriendClick = {
            friendRequestViewModel.deleteFriend(
                currentUserId = Firebase.auth.currentUser?.uid ?: "",
                friendId = userFoundInformationState.userId,
                onSuccess = {
                    friendRequestViewModel.sendRemoveNotification(
                        AcceptOrDeclineOrRemoveFriendDto(
                            currentUserInformation.username,
                            userFoundInformationState.fcmToken
                        )
                    )
                    Toast.makeText(
                        context,
                        "Friend Removed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        },
        userFoundInformation = userFoundInformationState
    )
}

@Composable
fun DateOfBirthInformationGuess(
    userFoundInformationState: UserFoundInformation,
    isDarkTheme: Boolean,
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
                text = " Date of birth:",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .offset(y = (3).dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatDate(userFoundInformationState.dateOfBirth),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .offset(y = (3).dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}