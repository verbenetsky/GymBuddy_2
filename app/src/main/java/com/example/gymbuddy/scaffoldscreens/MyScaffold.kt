package com.example.gymbuddy.scaffoldscreens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyScaffold(
    onProfileClick: () -> Unit,
    userManagementViewModel: UserManagementViewModel,
    onFriendsClick: () -> Unit,
    onMyWorkoutsClick: () -> Unit,
    onAIChatBotClick: () -> Unit,
    onMessageClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onProfileClick = onProfileClick,
                    onFriendsClick = onFriendsClick,
                    onMyWorkoutsClick = onMyWorkoutsClick,
                    onAIChatBotClick = onAIChatBotClick,
                    onMessageClick = onMessageClick,
                    onAboutClick = onAboutClick,
                    onLogoutClick = onLogoutClick,
                    drawerState = drawerState,
                    userManagementViewModel = userManagementViewModel,
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.appBarTitle,
                            )
                        }
                    },
                    modifier = modifier.clip(
                        RoundedCornerShape(
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Icon")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onSearchClick() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            content = { paddingValues ->
                content(paddingValues)
            }
        )
    }
}

@Composable
fun DrawerContent(
    onProfileClick: () -> Unit,
    drawerState: DrawerState,
    userManagementViewModel: UserManagementViewModel,
    onFriendsClick: () -> Unit,
    onMyWorkoutsClick: () -> Unit,
    onAIChatBotClick: () -> Unit,
    onMessageClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val userInformationState by userManagementViewModel.userInformationState.collectAsState()
    val scope = rememberCoroutineScope()

    Spacer(modifier = Modifier.height(16.dp))

    Image(
        painter = if (userInformationState.profilePictureUrl != "") rememberAsyncImagePainter(
            userInformationState.profilePictureUrl
        ) else painterResource(id = R.drawable.default_profile_picture),
        contentDescription = "Profile picture",
        modifier = Modifier
            .size(90.dp)
            .padding(10.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )

    Text(
        text = userInformationState.firstName + " " + userInformationState.lastName,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
        style = MaterialTheme.typography.displayMedium
    )
    Text(
        text = "@" + userInformationState.username,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(thickness = 1.dp)
    Spacer(modifier = Modifier.height(8.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Profile Icon"
            )
        },
        label = { Text(text = stringResource(R.string.my_profile)) },
        selected = false,
        onClick = {
            onProfileClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(imageVector = Icons.Default.Groups, contentDescription = "Friends Icon")
        },
        label = { Text(text = stringResource(R.string.my_friends)) },
        selected = false,
        onClick = {
            onFriendsClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Gym Icon")
        },
        label = { Text(text = stringResource(R.string.my_workouts)) },
        selected = false,
        onClick = {
            onMyWorkoutsClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(imageVector = Icons.Default.Android, contentDescription = "Android Icon")
        },
        label = { Text(text = stringResource(R.string.ai_motivator_chatbot)) },
        selected = false,
        onClick = {
            onAIChatBotClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Message Icon"
            )
        },
        label = { Text(text = stringResource(R.string.messages)) },
        selected = false,
        onClick = {
            onMessageClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(imageVector = Icons.Default.Info, contentDescription = "About Icon")
        },
        label = { Text(text = stringResource(R.string.about)) },
        selected = false,
        onClick = {
            onAboutClick()
            scope.launch { drawerState.close() }
        }
    )

    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider(thickness = 1.dp)
    Spacer(modifier = Modifier.height(4.dp))

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout Icon"
            )
        },
        label = { Text(text = stringResource(R.string.logout)) },
        selected = false,
        onClick = {
            onLogoutClick()
            scope.launch { drawerState.close() }
        }
    )
}
