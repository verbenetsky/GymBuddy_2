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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.gymbuddy.data.authentication.GoogleAuthUiClient
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.navigation.NavGraph
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import com.google.android.gms.auth.api.identity.Identity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.gymbuddy.buttonState.ButtonStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            GymBuddyTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    val navController = rememberNavController()
                    val signInViewModel: SignInViewModel = viewModel()
                    val userManagementViewModel: UserManagementViewModel = viewModel()
                    val userSearchViewModel: UserSearchViewModel = viewModel()
                    val authState = signInViewModel.authState.collectAsStateWithLifecycle()
                    val buttonStateManager = ButtonStateManager

                    NavGraph(
                        navController = navController,
                        signInViewModel = signInViewModel,
                        userManagementViewModel = userManagementViewModel,
                        userSearchViewModel = userSearchViewModel,
                        googleAuthUiClient = googleAuthUiClient,
                        lifecycleScope = lifecycleScope,
                        applicationContext = applicationContext,
                        buttonStateManager = buttonStateManager,
                        authState = authState.value
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
