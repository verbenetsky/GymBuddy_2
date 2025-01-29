package com.example.gymbuddy

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymbuddy.data.authentication.GoogleAuthUiClient
import com.example.gymbuddy.data.authentication.SignInScreen
import com.example.gymbuddy.data.authentication.SignInScreen2
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.presentation.ProfileScreen
import com.example.gymbuddy.data.authentication.SignUpScreen
import com.example.gymbuddy.data.authentication.UserInformationViewModel
import com.example.gymbuddy.presentation.RegistrationScreen
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult

@Composable
fun NavGraph(
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    userInformationViewModel: UserInformationViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context
) {
    NavHost(navController = navController, startDestination = "sign_in") {
        composable("sign_in") {
            val state by signInViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (googleAuthUiClient.getSignedInUser() != null)
                    navController.navigate("profile")
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        lifecycleScope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            signInViewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Sign in successful",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("profile")
                    signInViewModel.resetState()
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
                viewModel = signInViewModel,
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = signInViewModel,
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {
                    signInViewModel.clearLoginForm()
                    if (googleAuthUiClient.getSignedInUser() != null) {
                        lifecycleScope.launch {
                            googleAuthUiClient.signOut()
                            Toast.makeText(
                                applicationContext,
                                "Signed out",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        signInViewModel.signOut()
                        navController.navigate("sign_in")
                    } else {
                        signInViewModel.signOut()
                    }
                }
            )
        }

        composable("sign_up") {
            SignUpScreen(
                viewModel = signInViewModel,
                onCreateAnAccountClick = {
                    navController.navigate("registration")
                },
                onHaveAnAccountClick = { navController.navigate("sign_in") }
            )
        }

        composable("sign_in_2") {
            SignInScreen2(
                onForgetPasswordClick = { },
                onDontHaveAnAccountClick = { navController.navigate("sign_up") },
                viewModel = signInViewModel,
                onEditClick = { navController.navigate("sign_in") },
                onLoginSuccess = { navController.navigate("profile") },
            )
        }

        composable("registration") {
            RegistrationScreen(
                userInformationViewModel = userInformationViewModel,
                onContinueClick = { },
            )
        }
    }
}
