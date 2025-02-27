package com.example.gymbuddy.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ChatScreen(
    userManagementViewModel: UserManagementViewModel,
    channelID: String,
    modifier: Modifier = Modifier
) {
    val userInformation = userManagementViewModel.userInformationState.collectAsState()
    val viewModel: ChatViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.listenForMessages(channelID)
    }
    val messages = viewModel.message.collectAsState()

    ChatListMessages(
        messages = messages.value,
        onSendMessage = { message ->
            viewModel.sendMessage(
                channelID,
                message,
                userInformation.value.username
            )
        }
    )

}

@Composable
fun ChatListMessages(messages: List<Message>, onSendMessage: (String) -> Unit) {
    val msg = remember { mutableStateOf("") }
    val hideKeyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.BottomCenter)
                .background(Color(0xFF462A00)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {
            }) {
                Icon(
                    imageVector = Icons.Default.Attachment,
                    contentDescription = "Attach file",
                    modifier = Modifier.size(30.dp)
                )
            }

            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color(0xFF462A00),
                    unfocusedContainerColor = Color(0xFF462A00),
                    focusedIndicatorColor = Color(0xFF462A00),
                    unfocusedIndicatorColor = Color(0xFF462A00)
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        hideKeyboardController?.hide()
                    }
                )
            )
            IconButton(onClick = {
                if (msg.value.isNotEmpty())
                    onSendMessage(msg.value)
                msg.value = ""
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid

    val bubbleColor = if (isCurrentUser) {
        Color(0xFF8A6F4A)
    } else {
        Color(0xFF855400).copy(alpha = 0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Row(
            modifier = Modifier
                .padding(end = 8.dp, top = 4.dp)
                .align(alignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCurrentUser) {
                Image(
                    painter = painterResource(id = R.drawable.man_user_circle_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(40.dp)
                )
            }
            Text(
                text = message.message.trim(),
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)

            )
        }
    }
}