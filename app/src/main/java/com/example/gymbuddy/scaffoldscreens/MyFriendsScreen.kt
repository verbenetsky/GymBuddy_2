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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun MyFriendsScreen(
    friendRequestViewModel: FriendRequestViewModel,
    onAcceptClick: (UserFoundInformation) -> Unit,
    onDeclineClick: (UserFoundInformation) -> Unit,
    onSeeProfileClick: (String) -> Unit,
    onRemoveFriendClick: (UserFoundInformation) -> Unit,
    modifier: Modifier = Modifier
) {
    val friendRequestFullList by friendRequestViewModel.friendsRequestFullList.collectAsState()
    val friendList by friendRequestViewModel.friendList.collectAsState()

    var alertDialogState by remember { mutableStateOf(false) }
    var myFriendsSubScreen by remember { mutableStateOf(false) }
    var myInvitationSubScreen by remember { mutableStateOf(true) }
    var friendToRemove by remember { mutableStateOf<UserFoundInformation?>(null) }
    var friendToVisit by remember { mutableStateOf<UserFoundInformation?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
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
                shape = RoundedCornerShape(4.dp),
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
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    Color(0xFF855305).copy(alpha = 0.7f)
                )
            ) {
                Text(text = "Invitation", fontSize = 13.sp, color = Color.White)
            }
        }
        if (myInvitationSubScreen) {
            if (friendRequestFullList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)
                }
            } else {
                LazyColumn {
                    items(friendRequestFullList) { friend ->
                        val painter =
                            if (friend.profilePictureUrl.isEmpty()) painterResource(R.drawable.default_profile_picture) else rememberAsyncImagePainter(
                                friend.profilePictureUrl
                            )
                        SingleRecordOfFriendsList(
                            onSeeProfileClick = { onSeeProfileClick(friend.userId) },
                            onAcceptClick = { onAcceptClick(friend) },
                            onDeclineClick = { onDeclineClick(friend) },
                            painter = painter,
                            firstName = friend.firstName,
                            lastName = friend.lastName,
                            username = friend.username,
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
                            onSeeProfileClick = {
                                onSeeProfileClick(friend.userId)
                            },
                            onSendMessageClick = { },
                            onDeclineClick = { },
                            painter = painter,
                            onAlertDialogStateChange = { newValue ->
                                alertDialogState = newValue
                                friendToRemove = friend
                            },
                            firstName = friend.firstName,
                            lastName = friend.lastName,
                            username = friend.username,
                        )
                    }
                }
            }
        }
        friendToRemove?.let { friend ->
            AlertDialogDeleteFriend(
                dialogState = alertDialogState,
                changeDialogState = { newValue -> alertDialogState = newValue },
                onRemoveFriendClick = { onRemoveFriendClick(friend) },
                userFoundInformation = friend,
            )
        }
    }
}
// todo jak sie akceptuje zaproszenie i odrazu sie przechodzi do My Friends to nie ma nowego znajomego
// todo trezba przeladowac zeby sie pojawilo (czyli jescse raz kliknac na zakladke My Friends)


@Composable
fun SingleRecordOfFriendsList(
    onSeeProfileClick: () -> Unit,
    onAcceptClick: () -> Unit = {},
    onSendMessageClick: () -> Unit = {},
    onAlertDialogStateChange: (Boolean) -> Unit = {},
    myInvitationList: Boolean = true,
    firstName: String,
    lastName: String,
    painter: Painter,
    username: String,
    onDeclineClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .size(125.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = { if (myInvitationList) onAcceptClick() else onSendMessageClick() },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            Color(color = 0x0F0D791C).copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (myInvitationList) "Accept" else "Send Message",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (myInvitationList) onDeclineClick() else {
                                onAlertDialogStateChange(true)
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSeeProfileClick,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFF855305).copy(alpha = 0.7f)
                        )
                    ) {
                        Text(text = "See Profile", fontSize = 11.sp, color = Color.White)
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

