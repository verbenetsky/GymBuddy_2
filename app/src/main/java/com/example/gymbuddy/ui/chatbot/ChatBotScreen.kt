package com.example.gymbuddy.ui.chatbot

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.example.gymbuddy.R
import com.example.gymbuddy.data.model.ChatBotMessage
import com.example.gymbuddy.ui.messages.dateConverter
import com.example.gymbuddy.ui.theme.surfaceDark
import kotlinx.coroutines.delay

@Composable
fun ChatBotScreen(
    chatBotViewModel: ChatBotViewModel,
) {
    val messages = chatBotViewModel.messageListChatBot.collectAsState()
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ChatListMessages(
            messages = messages.value,
            onSendMessage = { message ->
                chatBotViewModel.sendMessage(message)
            },
            listState = listState,
            chatBotViewModel = chatBotViewModel
        )
    }
}

@Composable
fun ChatListMessages(
    messages: List<ChatBotMessage>,
    onSendMessage: (String) -> Unit,
    chatBotViewModel: ChatBotViewModel,
    listState: LazyListState
) {
    val msg = remember { mutableStateOf("") }
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val chatBotState = chatBotViewModel.chatBotResponseState.collectAsState()

    val showPlaceholder = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        showPlaceholder.value = true
    }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (messages.isEmpty() && showPlaceholder.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else surfaceDark)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(250.dp))
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(150.dp)
                )
                Text(
                    text = "Ask me anything!",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (chatBotState.value == ChatBotResponseState.Typing) {
                item(key = "typing") {
                    ChatBubble(
                        message = ChatBotMessage(
                            message = "Typing...",
                            role = "model",
                            createdAt = System.currentTimeMillis()
                        ),
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )
                }
            }
            items(
                items = messages.reversed(),
                key = { it.createdAt }
            ) { message ->
                SelectionContainer {
                    ChatBubble(
                        message = message,
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.BottomCenter)
                .background(if (isSystemInDarkTheme()) Color(0xFF462A00) else Color(0xFFFFF8F4)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {

            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),

                placeholder = {
                    Text(
                        text = "Type a message",
                        color = if (!isSystemInDarkTheme()) Color.Black else LocalContentColor.current
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF462A00) else Color(
                        0xFFFFF8F4
                    ),
                    unfocusedContainerColor = if (isSystemInDarkTheme()) Color(0xFF462A00) else Color(
                        0xFFFFF8F4
                    ),
                    focusedIndicatorColor = if (isSystemInDarkTheme()) Color(0xFF462A00) else Color(
                        0xFFFFF8F4
                    ),
                    unfocusedIndicatorColor = if (isSystemInDarkTheme()) Color(0xFF462A00) else Color(
                        0xFFFFF8F4
                    )
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        hideKeyboardController?.hide()
                    }
                )
            )
            IconButton(onClick = {
                if (msg.value.isNotEmpty()) {
                    onSendMessage(msg.value)
                    msg.value = ""
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}


@Composable
fun ChatBubble(
    message: ChatBotMessage,
    modifier: Modifier = Modifier
) {
    val isCurrentUser = message.role == "user"
    val bubbleColor = if (isCurrentUser) Color(0xFF8A6F4A) else Color(0xFF855400).copy(alpha = 0.8f)
    val screenWidth = LocalConfiguration.current.screenWidthDp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Row(
            modifier = Modifier
                .padding(end = 8.dp)
                .align(alignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCurrentUser) {
                Image(
                    painter = painterResource(id = R.drawable.chat_bot),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(40.dp)
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        bubbleColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .widthIn(max = (screenWidth * 0.7f).dp)
            ) {
                Column {
                    if (!isCurrentUser) {
                        // Renderowanie wiadomości jako HTML
                        AndroidView(
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    // Konwertujemy HTML na Spanned przy użyciu HtmlCompat
                                    this.text = HtmlCompat.fromHtml(
                                        message.message.trim(),
                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                    )
                                    setTextColor(Color.White.toArgb())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Wyświetlanie daty wiadomości
                        Text(
                            text = dateConverter(message.createdAt),
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .align(Alignment.Start)
                        )
                    } else {
                        Column {
                            Text(message.message.trim(), color = Color.White)
                            Text(
                                text = dateConverter(message.createdAt),
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp),
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}



