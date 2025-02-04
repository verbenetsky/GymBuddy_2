package com.example.gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.gymbuddy.data.authentication.GoogleAuthUiClient
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.navigation.NavGraph
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import com.google.android.gms.auth.api.identity.Identity


class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    val authState = signInViewModel.authState.collectAsStateWithLifecycle()

                    NavGraph(
                        navController = navController,
                        signInViewModel = signInViewModel,
                        userManagementViewModel = userManagementViewModel,
                        googleAuthUiClient = googleAuthUiClient,
                        lifecycleScope = lifecycleScope,
                        applicationContext = applicationContext,
                        authState = authState.value
                    )
                }
            }
        }
    }
}
