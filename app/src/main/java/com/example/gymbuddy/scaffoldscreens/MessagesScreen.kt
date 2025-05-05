@file:OptIn(ExperimentalMaterialApi::class)

package com.example.gymbuddy.scaffoldscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import com.example.gymbuddy.ui.theme.surfaceContainerLowDark
import com.example.gymbuddy.ui.theme.surfaceDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    channelViewModel: ChannelViewModel = hiltViewModel(),
    friendRequestViewModel: FriendRequestViewModel,
    userSearchViewModel: UserSearchViewModel,
    chatViewModel: ChatViewModel,
    innerNavController: NavController,
    modifier: Modifier = Modifier
) {
    val addChannel = remember { mutableStateOf(false) }
    val uid = Firebase.auth.currentUser?.uid ?: return
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val channelStatus by channelViewModel.channels.collectAsState() // lista wsyzstkich chatow

    val friendsList by friendRequestViewModel.friendList.collectAsState() // lista wszystkich znajomych

    // lista osob z ktorymi mozna rozpoczac czat
    val friendsWithoutChat by channelViewModel.friendsWithoutChat.collectAsState()

    LaunchedEffect(Unit) {
        println("lista of friends: $friendsList")
    }

    LaunchedEffect(friendsList) {
        channelViewModel.refreshFriendsWithoutChat(friendsWithoutChat.toList())
    }

    DisposableEffect(uid) {
        friendRequestViewModel.startListeningFriends(uid)
        onDispose { friendRequestViewModel.stopListeningFriends() }
    }

    LaunchedEffect(friendsList, channelStatus) {
        channelViewModel.refreshFriendsWithoutChat(friendsList)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (friendsList.isNotEmpty() && friendsWithoutChat.isNotEmpty())
                        addChannel.value = true
                    else if (friendsWithoutChat.isEmpty() && friendsList.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "You have already started chats with all of your friends.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "You don't have a single friend, please add some friends first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                contentColor = if (!isSystemInDarkTheme()) Color.White else surfaceContainerLowDark
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Message"
                )
            }
        },
        content = { paddingValues ->
            if (channelStatus.isEmpty()) {
                Box(
                    modifier = Modifier
                        .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
                        .fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing here", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
                        .padding(paddingValues)

                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(channelStatus.toList()) { channel ->
                            val friendId =
                                if (channel.secondFriendId == Firebase.auth.currentUser!!.uid)
                                    channel.firstFriendId
                                else
                                    channel.secondFriendId

                            val userInfo by produceState<UserFoundInformation?>(
                                initialValue = null,
                                key1 = friendId
                            ) {
                                value = userSearchViewModel.getUserBasedOnUserId(friendId)
                                value?.let { userSearchViewModel.updateUserFoundInformation(it) }
                            }

                            if (userInfo == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                val painter = rememberAsyncImagePainter(
                                    model = userInfo!!.profilePictureUrl,
                                    error = painterResource(R.drawable.default_profile_picture)
                                )

                                val lastMessage by produceState<String?>(
                                    initialValue = null,
                                    key1 = channel.id
                                ) {
                                    channelViewModel.getLastMessage(channel.id) { message ->
                                        value = message
                                    }
                                }

                                val displayLastMessage =
                                    if (lastMessage == null || lastMessage!!.isEmpty())
                                        ""
                                    else
                                        lastMessage

                                SwipeToDeleteContainer(
                                    item = channel, // tutaj sie wyznacza ten typ
                                    onDelete = { deletedChannel ->

                                        chatViewModel.deleteAllImages(deletedChannel.id) // usuwamy wszystkie zdjecia

                                        coroutineScope.launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Deleted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        channelViewModel.deleteChannel( // usuwamy channel
                                            channelId = deletedChannel.id,
                                            onSuccess = {
                                                chatViewModel.deleteAllMessages(
                                                    deletedChannel.id
                                                )
                                            } // usuwamy wszystkie wiadomosci
                                        )
                                    }
                                ) { ch -> // content, typu Channel gdyz jest to generic
                                    println("channel id : ${ch.id}")
                                    if (displayLastMessage != null) {
                                        ChannelItem(
                                            firstName = userInfo!!.firstName,
                                            lastName = userInfo!!.lastName,
                                            painter = painter,
                                            lastMessage = displayLastMessage
                                        ) { // onClickChannelItem =
                                            println("user info: $userInfo")
                                            innerNavController.navigate("chat/${ch.id}/${userInfo!!.userId}")
                                            val fullName = userInfo!!.firstName + userInfo!!.lastName
                                            chatViewModel.updateCurrentChatName(fullName)

                                            coroutineScope.launch {
                                                userSearchViewModel.getUserBasedOnUserId(userInfo!!.userId)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    if (addChannel.value) {
        ModalBottomSheet(
            onDismissRequest = { addChannel.value = false },
            sheetState = sheetState,
        ) {
            AddChannelDialog(friendsList = friendsWithoutChat.toList(), onAddChannel = {
                channelViewModel.addChannel(it)
                addChannel.value = false
            })
        }
    }
}

@Composable
fun ChannelItem(
    firstName: String,
    lastName: String,
    painter: Painter,
    lastMessage: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (!isSystemInDarkTheme()) Color(0xFF855400).copy(alpha = 0.5f) else Color(
                    0xFF462A00
                )
            )
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .size(75.dp)
                .background(Color(0xFFFFCA89).copy(alpha = 0.3f))
        ) {
            Image(
                painter = painter,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = ""
            )
        }
        Column {
            Text(
                text = "$firstName $lastName",
                modifier = Modifier
                    .padding(8.dp)
                    .offset(y = 13.dp),
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp)
            )
            Text(
                text = lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .padding(8.dp),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
fun AddChannelDialog(
    friendsList: List<UserFoundInformation>,
    onAddChannel: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pick a Friend to start a Chat",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 25.sp)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(friendsList) { friend ->
                SingleRecordOfFriendsLists(
                    userFoundInformation = friend,
                    onRecordClick = {
                        onAddChannel(friend.userId)
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun SingleRecordOfFriendsLists(
    userFoundInformation: UserFoundInformation,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter =
        if (userFoundInformation.profilePictureUrl.isEmpty()) painterResource(R.drawable.default_profile_picture) else rememberAsyncImagePainter(
            userFoundInformation.profilePictureUrl
        )
    Card(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .size(70.dp)
            .clickable {
                onRecordClick()
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
                    .size(70.dp)
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
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
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    animationDuration: Int = 500,
    content: @Composable (T) -> Unit
) {
    var isRemovedCompletely by remember { mutableStateOf(false) }
    var isTryingToRemove by remember { mutableStateOf(false) }
    val state = rememberDismissState(
        confirmStateChange = { value ->
            if (value == DismissValue.DismissedToStart) {
                isTryingToRemove = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(key1 = isRemovedCompletely) {
        if (isRemovedCompletely) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    LaunchedEffect(key1 = isTryingToRemove) {
        if (!isTryingToRemove && state.currentValue == DismissValue.DismissedToStart) {
            state.reset()
        }
    }

    AnimatedVisibility(
        visible = !isRemovedCompletely,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismiss(state = state, background = {
            DeleteBackground(swipeDismissState = state)
        }, dismissContent = {
            content(item)
        },
            directions = setOf(DismissDirection.EndToStart)
        )
    }
    AlertDialogDeleteChat(
        dialogState = isTryingToRemove,
        changeDialogState = { newValue -> isTryingToRemove = newValue },
        onDelete = {
            isRemovedCompletely = true
        }
    )
}

@Composable
fun DeleteBackground(swipeDismissState: DismissState) {
    val color = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart) {
        Color.Red
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(end = 30.dp)
        )
    }
}

@Composable
fun AlertDialogDeleteChat(
    dialogState: Boolean,
    changeDialogState: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    if (dialogState) {
        AlertDialog(
            onDismissRequest = {
                changeDialogState(false)
            },
            title = {
                Text(text = "Delete Chat")
            },
            text = {
                Text("Are you sure you want to delete this chat?\nThe chat will be deleted for both participants.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        changeDialogState(false)
                        onDelete()
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