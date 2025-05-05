package com.example.gymbuddy.scaffoldscreens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageScope
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.data.authentication.UserSearchViewModel.UserSearchState
import com.example.gymbuddy.friends.FriendRequestInformationDto
import com.example.gymbuddy.pushnotification.AcceptOrDeclineOrRemoveFriendDto
import com.example.gymbuddy.pushnotification.FriendRequestDto
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import com.example.gymbuddy.ui.theme.surfaceDark
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.map

@Composable
fun SearchScreen(
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    onGuestProfileClick: () -> Unit,
    onProfileClick: () -> Unit,
    userSearchViewModel: UserSearchViewModel,
    userManagementViewModel: UserManagementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val uid = Firebase.auth.currentUser?.uid ?: return

    val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState() // info about found user
    val currentUserInformation by userManagementViewModel.userInformationState.collectAsState() // info about found user

    var alertDialogState by remember { mutableStateOf(false) }

    val userSearchState by userSearchViewModel.userSearchState.collectAsState()

    val searchFieldValue by userSearchViewModel.searchFieldValue.collectAsState()

    // val buttonState by buttonStateManager.buttonState.collectAsState()

    // za kazdym razem jak powracamy do ekranu search to zerujemy stan zeby uniknac takiej sytuacji ze
    // zostanie wyszukany jakis user (friend) ale w polu searchFieldValue jest pusto


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (!isDarkTheme) MaterialTheme.colorScheme.surface else surfaceDark )
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        OutlinedTextField(
            value = searchFieldValue,
            onValueChange = { userSearchViewModel.updateSearchField(it) },
            singleLine = true,
            label = { Text(text = "Enter user's username to search") },
            trailingIcon = {
                IconButton(onClick = {
                    userSearchViewModel.searchUser(
                        username = searchFieldValue,
                        onSuccess = {
                            userSearchViewModel.updateSearchState(UserSearchState.FoundUser)
                        },
                        onFailure = {
                            Toast.makeText(
                                context,
                                "Nothing found",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onNoOneFound = {
                            Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // jesli znajdziemy konkretnego usera to mamy kilka opcji do wyboru:
        if (userSearchState == UserSearchState.FoundUser) {

            DisposableEffect(uid, userFoundInformation.userId) {
                friendRequestViewModel.startObservingButton(uid, userFoundInformation.userId)
                onDispose { friendRequestViewModel.stopObservingButton() }
            }

            // Teraz możesz bezpiecznie kolekcjonować stan przycisku
            val buttonState by friendRequestViewModel.buttonState
                .map { it?.name ?: "" }
                .collectAsState(initial = null)

            if (buttonState != null) {
                SingleRecordOfSearch(
                    onProfileClick = onProfileClick,
                    // mozemy wyslac request (jesli nie mamy zadnej relacji z tym userem)
                    onSendRequestClick = {
                        val friendRequestInformationDto = FriendRequestInformationDto(
                            receiverId = userFoundInformation.userId,
                            date = System.currentTimeMillis(),
                            senderId = Firebase.auth.currentUser?.uid ?: "",
                        )
                        friendRequestViewModel.sendFriendRequest(
                            friendRequestDto = friendRequestInformationDto,
                            onSuccess = {
                                friendRequestViewModel.sendFriendRequestToUser(
                                    FriendRequestDto(
                                        userFoundInformation.fcmToken
                                    )
                                ) // wysylanie powiadomienia
                                Toast.makeText(
                                    context,
                                    "Friend request successfully sent",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = { e ->
                                println("Something gone wrong: $e")
                                Toast.makeText(
                                    context,
                                    "Something happened, try again!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    userFoundInformation = userFoundInformation,
                    buttonState = buttonState!!,
                    onGuestProfileClick = onGuestProfileClick,

                    // mozemy odrzucic request (jesli do nas wyslali zaproszenie)
                    onDeclineClick = {

                        friendRequestViewModel.declineFriendRequest(
                            currentUserId = Firebase.auth.currentUser?.uid ?: "",
                            friendId = userFoundInformation.userId,
                            onSuccess = {
                                friendRequestViewModel.sendDeclineNotification(
                                    AcceptOrDeclineOrRemoveFriendDto(
                                        currentUserInformation.username,
                                        userFoundInformation.fcmToken
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "Friend request declined",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = {
                                println("declining failed")
                            }
                        )
                        userSearchViewModel.searchUser(
                            username = userFoundInformation.username,
                            onSuccess = {
                                userSearchViewModel.updateSearchState(UserSearchState.FoundUser)
                            },
                        )
                    },

                    // mozemy usunac ze znajomych (jesli mamy w znajomych)
                    onRemoveClick = {
                        alertDialogState = true
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (userSearchState) {
                    UserSearchState.NothingFound ->
                        Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)

                    UserSearchState.Loading ->
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
            onRemoveFriendClick = {
                friendRequestViewModel.deleteFriend(
                    currentUserId = Firebase.auth.currentUser?.uid ?: "",
                    friendId = userFoundInformation.userId,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Friend Removed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                friendRequestViewModel.sendRemoveNotification(
                    AcceptOrDeclineOrRemoveFriendDto(
                        it.username,
                        it.fcmToken
                    )
                )
            },
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
    buttonState: String,
    userFoundInformation: UserFoundInformation,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    Card(
        modifier = modifier
            .fillMaxWidth()
            .size(200.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
        ) {

            SubcomposeAsyncImage(
                model = userFoundInformation.profilePictureUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }, error = {

                    Image(
                        painter = painterResource(id = R.drawable.default_profile_picture),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            )

//            Image(
//                painter = painter,
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(200.dp)
//                    .aspectRatio(1f)
//            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (userFoundInformation.userId != userId) {
                        Button(
                            onClick = {
                                when (buttonState) {
                                    "Remove" -> onRemoveClick()
                                    "Decline" -> onDeclineClick()
                                    else -> onSendRequestClick()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth(),
                            enabled = buttonState == "SendRequest" || buttonState == "Send Message" || buttonState == "Remove" || buttonState == "Decline",
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                when (buttonState) {
                                    "SendRequest" -> Color(0xFF06B936).copy(alpha = 0.7f)
                                    "Remove" -> Color(0xFFD31212)
                                    "Decline" -> Color(0xFFD31212)
                                    else -> Color(0xFFFFCA89)
                                }
                            )
                        ) {
                            Text(
                                text = buttonState,
                                fontSize = 13.sp,
                                color = when (buttonState) {
                                    "SendRequest" -> Color.White
                                    "Remove" -> Color.White
                                    "Decline" -> Color.White
                                    else -> LocalContentColor.current
                                }
                            )
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (userFoundInformation.userId == userId) {
                                onProfileClick()
                            } else
                                onGuestProfileClick()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth(),
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

