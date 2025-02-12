package com.example.gymbuddy.scaffoldscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.ui.theme.appBarTitle

@Composable
fun SearchScreen(
    userSearchState: UserSearchViewModel.UserSearchState,
    onSearchClick: () -> Unit,
    userSearchViewModel: UserSearchViewModel,
    modifier: Modifier = Modifier
) {
    val userSearchQuery by userSearchViewModel.searchQuery.collectAsState()
    val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState()

    LaunchedEffect(userSearchState, userFoundInformation) {
        println(userFoundInformation)
        println(userSearchState)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 16.dp)
    ) {
        OutlinedTextField(
            value = userSearchQuery,
            onValueChange = { userSearchViewModel.updateUserSearchQuery(it) },
            singleLine = true,
            label = { Text(text = "Enter user's username to search") },
            trailingIcon = {
                IconButton(onClick = { onSearchClick() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (userSearchState == UserSearchViewModel.UserSearchState.FoundUser) {
            SingleRecordOfSearch(userSearchViewModel)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (userSearchState) {
                    UserSearchViewModel.UserSearchState.NothingFound ->
                        Text(text = "Nothing found", style = MaterialTheme.typography.appBarTitle)

                    UserSearchViewModel.UserSearchState.Loading ->
                        CircularProgressIndicator()

                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}


@Composable
fun SingleRecordOfSearch(userSearchViewModel: UserSearchViewModel, modifier: Modifier = Modifier) {

    val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .size(100.dp)
            .clickable {

            }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var painter = painterResource(id = R.drawable.default_profile_picture)
            if (userFoundInformation.profilePictureUrl.isNotEmpty()) {
                painter = rememberAsyncImagePainter(userFoundInformation.profilePictureUrl)
            }
            println(painter)

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
            }
        }
    }
}
