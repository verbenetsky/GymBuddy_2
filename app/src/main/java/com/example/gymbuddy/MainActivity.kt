package com.example.gymbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymbuddy.data.authentication.GoogleAuthUiClient
import com.example.gymbuddy.data.authentication.SignInScreen
import com.example.gymbuddy.data.authentication.SignInScreen2
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.presentation.ProfileScreen
import com.example.gymbuddy.data.authentication.SignUpScreen
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

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
                    val viewModel = viewModel<SignInViewModel>()
                    val userData by viewModel.userData.collectAsState(initial = null)

                    println("here !$userData")
                    NavHost(navController = navController, startDestination = "sign_in") {

                        composable("sign_in") {

                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null)
                                    navController.navigate("profile")
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data
                                                    ?: return@launch// jesli intent jest null to wychodzimy z korutyny launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                println("isSignInSuccessful: ${state.isSignInSuccessful}")

                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }

                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }

                                },
                                onSignUpClick = {
                                    navController.navigate("sign_up")
                                },
                                onContinueSignInScreenClick = { navController.navigate("sign_in_2") },
                                viewModel = viewModel,
                            )
                        }
                        composable(route = "profile") {

                            ProfileScreen(
                                viewModel = viewModel,
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    viewModel.clearLoginForm()
                                    if (googleAuthUiClient.getSignedInUser() != null) {
                                        lifecycleScope.launch {
                                            googleAuthUiClient.signOut()
                                            Toast.makeText(
                                                applicationContext,
                                                "Signed out",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        viewModel.signOut()
                                        navController.navigate("sign_in")
                                    } else {
                                        viewModel.signOut()
                                    }

                                })
                        }

                        composable(route = "sign_up") {
                            val loginFormState by viewModel.loginFormState.collectAsState()
                            SignUpScreen(
                                viewModel = viewModel,
                                onCreateAnAccountClick = {
                                    viewModel.signUp(
                                        email = loginFormState.email,
                                        password = loginFormState.password,
                                        onSuccess = {
                                            navController.navigate("profile")
                                        },
                                        onError = {}

                                    )
                                },
                                onHaveAnAccountClick = { navController.navigate("sign_in") },
                                navigateToProfile = { navController.navigate("profile") }
                            )
                        }

                        composable(route = "sign_in_2") {
                            SignInScreen2(
                                onForgetPasswordClick = { },
                                onDontHaveAnAccountClick = { navController.navigate("sign_up") },
                                viewModel = viewModel,
                                onEditClick = { navController.navigate("sign_in") },
                                onLoginSuccess = { navController.navigate("profile") },
                            )
                        }

                    }
                }
            }
        }
    }
}

