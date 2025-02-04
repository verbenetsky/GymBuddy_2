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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.example.gymbuddy.R
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.ui.theme.appBarTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onImageClick: () -> Unit,
    onMoreClick: () -> Unit,
    onEditClick: () -> Unit,
    userManagementViewModel: UserManagementViewModel,
    modifier: Modifier = Modifier
) {
    val userInformationState by userManagementViewModel.userInformationState.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onEditClick() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Account"
                    )
                }
                IconButton(onClick = { onMoreClick() }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More option"
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.default_profile_picture),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(125.dp)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            Text(
                text = userInformationState.firstName + " " + userInformationState.lastName,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.appBarTitle
            )


            Text(
                text = "@" + userInformationState.username,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))


            Card(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .padding(start = 8.dp),
                    value = userInformationState.email,
                    enabled = false,
                    singleLine = true,
                    onValueChange = { },
                    textStyle = MaterialTheme.typography.titleMedium,

                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.White, disabledLabelColor = Color.White,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                        )
                    },

                    label = { Text("Email") })


            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "    Date of birth:",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .offset(y = (3).dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatDate(userInformationState.dateOfBirth),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .offset(y = (3).dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { },
                        enabled = false,

                        ) { // todo
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Date Icon",
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp)
            ) {

                TextField(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .padding(start = 8.dp),
                    value = userInformationState.hobbies.joinToString(", "),
                    readOnly = true,
                    singleLine = true,
                    onValueChange = { },
                    maxLines = 2,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.White, disabledLabelColor = Color.White,
                    ),

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Email Icon"
                        )
                    },

                    label = { Text("Hobbies:") })
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp)
            ) {

                TextField(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .padding(start = 8.dp),
                    value = userInformationState.goal,
                    readOnly = true,
                    singleLine = true, // todo
                    onValueChange = { },
                    maxLines = 2,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.White, disabledLabelColor = Color.White,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Email Icon"
                        )
                    },

                    label = { Text("Goal:") })
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

@Composable
private fun formatDate(date: Long): String {
    val formattedDate = remember(date) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
            Date(date)
        )
    }
    return formattedDate
}
