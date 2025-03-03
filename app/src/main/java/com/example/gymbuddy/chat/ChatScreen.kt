package com.example.gymbuddy.chat

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import android.Manifest
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    userManagementViewModel: UserManagementViewModel,
    innerNavController: NavController,
    channelID: String,
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userInformation = userManagementViewModel.userInformationState.collectAsState()
    val viewModel: ChatViewModel = hiltViewModel()
    val chooserDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let {
                chatViewModel.sendImageMessage(it, channelID, userInformation.value.username)
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendImageMessage(it, channelID, userInformation.value.username)
        }
    }


    LaunchedEffect(Unit) {
        viewModel.listenForMessages(channelID)
    }

    val messages = viewModel.message.collectAsState()

    ChatListMessages(
        messages = messages.value,
        onSendMessage = { message ->
            viewModel.sendMessage(
                channelID = channelID,
                messageText = message,
                senderUsername = userInformation.value.username,
            )
        }, onImageClick = {
            chooserDialog.value = true
        }
    )

    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES).first()
        return FileProvider.getUriForFile(
            innerNavController.context,
            "${innerNavController.context.packageName}.provider",
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                cameraImageUri.value = Uri.fromFile(this)
            }
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                cameraImageLauncher.launch(createImageUri(context))
            }
        }



    if (chooserDialog.value) {
        ContentSelectionDialog(
            onCameraSelected = {
                chooserDialog.value = false
                if (innerNavController.context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    cameraImageLauncher.launch(createImageUri(context))
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallerySelected = {
                imageLauncher.launch("image/*")
                chooserDialog.value = false
            }
        )
    }
}


@Composable
fun ChatListMessages(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onImageClick: () -> Unit
) {
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
                onImageClick()
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
            .padding(vertical = 4.dp),
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
                    painter = painterResource(id = R.drawable.man_user_circle_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(40.dp)
                )
            }

            if (message.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                ) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "image",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.FillBounds // rozciaga troche ale niech bedzoe
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = message.message?.trim() ?: "",
                        color = Color.White,
                    )
                }
            }
        }

    }
}

@Composable
fun ContentSelectionDialog(onCameraSelected: () -> Unit, onGallerySelected: () -> Unit) {
    AlertDialog(
        onDismissRequest = {

        },
        confirmButton = {
            TextButton(onClick = { onCameraSelected() }) {
                Text(text = "Camera", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onGallerySelected() }) {
                Text(text = "Gallery", color = Color.White)
            }

        },
        title = {
            Text(text = "Select your source")
        },
        text = {
            Text(text = "Choose a source from which to retrieve an image")
        })
}