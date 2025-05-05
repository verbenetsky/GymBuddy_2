package com.example.gymbuddy.scaffoldscreens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.channel.ChannelViewModel
import com.example.gymbuddy.chat.ChatViewModel
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.pushnotification.AcceptOrDeclineOrRemoveFriendDto
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import com.example.gymbuddy.ui.theme.surfaceDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun MyFriendsScreen(
    friendRequestViewModel: FriendRequestViewModel,
    channelViewModel: ChannelViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    userSearchViewModel: UserSearchViewModel,
    innerNavController: NavController,
    onSeeProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val friendRequestList by friendRequestViewModel.friendsRequestList.collectAsState()
    val friendList by friendRequestViewModel.friendList.collectAsState()

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var alertDialogState by remember { mutableStateOf(false) }
    var myFriendsSubScreen by remember { mutableStateOf(false) }
    var myInvitationSubScreen by remember { mutableStateOf(true) }
    var friendToRemove by remember { mutableStateOf<UserFoundInformation?>(null) }
    val uid = Firebase.auth.currentUser?.uid ?: return

    //DisposableEffect to narzędzie do wykonywania kodu „przy wejściu” i „przy wyjściu” danego composable z drzewa UI.
    // stosuje sie gdy rejestrujesz listener, broadcast receiver, Flux/Flow w callbackFlow, itp., i musisz go potem ręcznie odpiąć
    DisposableEffect(uid) {
        friendRequestViewModel.startListeningForRequests(uid)
        friendRequestViewModel.startListeningFriends(uid)

        onDispose {
            // tutaj sprzatamy co zostalo utworzone w glownym bloku DisposableEffect
            // Uruchomi się gdy Composable przestaje być w drzewie (np. użytkownik opuści ekran)
            // lub gdy klucz się zmieni i Compose musi „odświeżyć” efekt.
            friendRequestViewModel.stopListening()
            friendRequestViewModel.stopListeningFriends()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = {
                    myFriendsSubScreen = !myFriendsSubScreen
                    myInvitationSubScreen = !myInvitationSubScreen
                },
                shape = RoundedCornerShape(10.dp),
                enabled = !myFriendsSubScreen,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    Color(0xFF855305).copy(alpha = 0.7f)
                )
            ) {
                Text(text = "My Friends", fontSize = 13.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    myInvitationSubScreen = !myInvitationSubScreen
                    myFriendsSubScreen = !myFriendsSubScreen
                },
                enabled = !myInvitationSubScreen,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    Color(0xFF855305).copy(alpha = 0.7f)
                )
            ) {
                Text(text = "Invitation", fontSize = 13.sp, color = Color.White)
            }
        }
        if (myInvitationSubScreen) {
            if (friendRequestList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)
                }
            } else {
                LazyColumn {
                    items(friendRequestList) { friend ->
                        val painter =
                            if (friend.profilePictureUrl.isEmpty()) painterResource(R.drawable.default_profile_picture) else rememberAsyncImagePainter(
                                friend.profilePictureUrl
                            )
                        SingleRecordOfFriendsList(
                            onCardClick = {
                                onSeeProfileClick()
                                userSearchViewModel.getUserBasedOnUserId(friend.userId) {}
                            },
                            onAcceptClick = {
                                //onAcceptClick(friend)
                                friendRequestViewModel.acceptFriendRequest(
                                    currentUserId = Firebase.auth.currentUser?.uid ?: "",
                                    friendId = friend.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "You've got a new friend",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )

                                friendRequestViewModel.sendAcceptNotification(
                                    acceptFriendRequestDto = AcceptOrDeclineOrRemoveFriendDto(
                                        senderName = friend.username,
                                        receiverFcmToken = friend.fcmToken
                                    )
                                )
                            },
                            onDeclineClick = {
                                //onDeclineClick(friend)
                                friendRequestViewModel.declineFriendRequest(
                                    currentUserId = Firebase.auth.currentUser?.uid ?: "",
                                    friendId = friend.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "You've declined friend request",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = {
                                        println("something wrong while declining friend request")
                                    }
                                )
                                friendRequestViewModel.sendDeclineNotification(
                                    declineFriendRequestDto = AcceptOrDeclineOrRemoveFriendDto(
                                        senderName = friend.username,
                                        receiverFcmToken = friend.fcmToken
                                    )
                                )
                            },
                            painter = painter,
                            firstName = friend.firstName,
                            lastName = friend.lastName,
                            username = friend.username,
                            isDarkTheme = isSystemInDarkTheme()
                        )
                    }
                }
            }
        } else if (myFriendsSubScreen) {
            if (friendList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)
                }
            } else {
                LazyColumn {
                    items(friendList) { friend ->
                        val painter =
                            if (friend.profilePictureUrl.isEmpty()) painterResource(R.drawable.default_profile_picture) else rememberAsyncImagePainter(
                                friend.profilePictureUrl
                            )
                        SingleRecordOfFriendsList(
                            myInvitationList = false,
                            onCardClick = {
                                onSeeProfileClick()
                                userSearchViewModel.getUserBasedOnUserId(friend.userId) {}
                            },
                            onSendMessageClick = {

                                scope.launch {
                                    // 1) probujemy znalezc kanal
                                    val existing = channelViewModel.findChannel(friend.userId)

                                    // 2) Jeśli nie ma, tworzymy nowy i pobieramy jego id
                                    val channelId = existing.ifEmpty {
                                        channelViewModel.addChannelSuspend(friend.userId)
                                    }

                                    // 3) Pobieramy dane znajomego
                                    val userInfo =
                                        userSearchViewModel.getUserBasedOnUserId(friend.userId)
                                    val fullName = "${userInfo?.firstName} ${userInfo?.lastName}"

                                    // 4) Ustawiamy tytuł czatu i nawigujemy
                                    chatViewModel.updateCurrentChatName(fullName)
                                    innerNavController.navigate("chat/$channelId/${friend.userId}")
                                }
                            },
                            painter = painter,
                            onAlertDialogStateChange = { newValue ->
                                alertDialogState = newValue
                                friendToRemove = friend
                            },
                            firstName = friend.firstName,
                            lastName = friend.lastName,
                            username = friend.username,
                            isDarkTheme = isSystemInDarkTheme()
                        )
                    }
                }
            }
        }
        friendToRemove?.let { friend ->
            AlertDialogDeleteFriend(
                dialogState = alertDialogState,
                changeDialogState = { newValue -> alertDialogState = newValue },
                onRemoveFriendClick = {
                    friendRequestViewModel.deleteFriend(
                        currentUserId = Firebase.auth.currentUser?.uid ?: "",
                        friendId = friend.userId,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Friend successfully deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    friendRequestViewModel.sendRemoveNotification(
                        removeDto = AcceptOrDeclineOrRemoveFriendDto(
                            senderName = friend.username,
                            receiverFcmToken = friend.fcmToken
                        )
                    )
                },
                userFoundInformation = friend,
            )
        }
    }
}

@Composable
fun SingleRecordOfFriendsList(
    isDarkTheme: Boolean,
    onAcceptClick: () -> Unit = {},
    onSendMessageClick: () -> Unit = {},
    onAlertDialogStateChange: (Boolean) -> Unit = {},
    myInvitationList: Boolean = true,
    firstName: String,
    lastName: String,
    painter: Painter,
    username: String,
    onDeclineClick: () -> Unit = {},
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .size(200.dp)
            .clickable {
                onCardClick()
            }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
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
                        text = firstName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.9f)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = lastName,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.appBarTitle.fontSize * 0.9f)
                    )
                }
                Row {
                    Text(
                        text = "@",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * 1.1)
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * 1.1)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { if (myInvitationList) onAcceptClick() else onSendMessageClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors =
                        ButtonDefaults.buttonColors(
                            if (myInvitationList) Color(color = 0x0F0D791C).copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (myInvitationList) "Accept" else "Send Message",
                            fontSize = 10.sp,
                            maxLines = 1,
                            color = if (isDarkTheme) {
                                if (myInvitationList) Color.White else Color.Black
                            } else {
                                Color.White
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (myInvitationList) onDeclineClick() else {
                                onAlertDialogStateChange(true)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFFD31212).copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = if (myInvitationList) "Decline" else "Remove",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.size(16.dp))
}

@Composable
fun AlertDialogDeleteFriend(
    dialogState: Boolean,
    changeDialogState: (Boolean) -> Unit,
    onRemoveFriendClick: (UserFoundInformation) -> Unit,
    userFoundInformation: UserFoundInformation,
) {
    if (dialogState) {
        AlertDialog(
            onDismissRequest = {
                changeDialogState(false)
            },
            title = {
                Text(text = "Remove Friend")
            },
            text = {
                Text("Are you sure you want to remove this friend?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFriendClick(userFoundInformation)
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

