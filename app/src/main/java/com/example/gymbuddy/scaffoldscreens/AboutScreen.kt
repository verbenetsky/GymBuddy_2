package com.example.gymbuddy.scaffoldscreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {

    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(R.string.about_app),
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.gym_buddy_r),
            style = MaterialTheme.typography.displayLarge,
        )

        Text(text = stringResource(R.string.git_hub_link),modifier = Modifier.clickable {
            uriHandler.openUri("https://github.com/goofydoog/GymBuddy_2")
        })
    }

}