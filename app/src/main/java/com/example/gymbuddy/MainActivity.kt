package com.example.gymbuddy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.gymbuddy.ui.auth.AuthViewModel
import com.example.gymbuddy.ui.profile.UserManagementViewModel
import com.example.gymbuddy.ui.search.UserSearchViewModel
import com.example.gymbuddy.navigation.NavGraph
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gymbuddy.ui.messages.ChatViewModel
import com.example.gymbuddy.ui.auth.CredentialManager
import com.example.gymbuddy.ui.auth.SignInViewModelFactory
import com.example.gymbuddy.data.repositoryImpl.AuthRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        val accountManager = CredentialManager(this)
        val factory = SignInViewModelFactory(
            authRepo = AuthRepositoryImpl(),
            accountManager = accountManager
        )
        enableEdgeToEdge()
        setContent {
            GymBuddyTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {

                    val signInViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

                    val navController = rememberNavController()
                    val userManagementViewModel: UserManagementViewModel = viewModel()
                    val userSearchViewModel: UserSearchViewModel = viewModel()
                    val chatViewModel: ChatViewModel = viewModel( )

                    NavGraph(
                        navController = navController,
                        signInViewModel = signInViewModel,
                        userManagementViewModel = userManagementViewModel,
                        userSearchViewModel = userSearchViewModel,
                        chatViewModel = chatViewModel,
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                )
            }
        }
    }
}
