package com.example.gymbuddy.scaffoldscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymbuddy.channel.ChannelViewModel
import kotlin.math.truncate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    channelViewModel: ChannelViewModel = hiltViewModel(),
    innerNavController: NavController,
    modifier: Modifier = Modifier
) {
    val channelStatus = channelViewModel.channels.collectAsState()
    val addChannel = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        println(channelStatus.value)
    }
    // tutaj ten scaffold teoretycznie nie jest potrzebny ale bez niego nie mozna uzyc FAB
    // teoretycznie mozna bylo zrealizowac FAB w MyScaffold pliku ale wole odseparowac rzeczy
    // i miec je w jednym miejscu
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addChannel.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Message"
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)

            ) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    item {
                        Text(
                            text = "Messages:",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    item {
                        TextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text(text = "Search...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    items(channelStatus.value) { channel ->
                        Column {
                            ChannelItem(channelName = channel.name) {
                                innerNavController.navigate("chat/${channel.id}")
                            }
                        }
                    }
                }
            }
        }
    )
    if (addChannel.value) {
        ModalBottomSheet(onDismissRequest = { addChannel.value = false }, sheetState = sheetState) {
            AddChannelDialog {
                channelViewModel.addChannel(it)
                addChannel.value = false
            }
        }
    }
}

@Composable
fun ChannelItem(channelName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF462A00))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFCA89).copy(alpha = 0.3f))
        ) {
            Text(
                text = channelName[0].toString().uppercase(),
                color = Color.White,
                style = TextStyle(fontSize = 35.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = channelName, modifier = Modifier
                .padding(8.dp), color = Color.White
        )
    }
}

@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    val channelName = remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(text = "Add Channel")
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(value = channelName.value, onValueChange = {
            channelName.value = it
        }, label = { Text(text = "Channel Name") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = { onAddChannel(channelName.value) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Add")
        }
    }
}