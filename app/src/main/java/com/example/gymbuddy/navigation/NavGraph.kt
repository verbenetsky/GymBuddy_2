package com.example.gymbuddy.navigation

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
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.presentation.RegistrationScreen
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gymbuddy.scaffoldscreens.AboutScreen
import com.example.gymbuddy.scaffoldscreens.MyScaffold

@Composable
fun NavGraph(
    authState: SignInViewModel.AuthState,
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    userManagementViewModel: UserManagementViewModel,
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
                onContinueSignInScreenClick = {
                    navController.navigate("sign_in_2")
                    signInViewModel.resetPassword()
                },
                viewModel = signInViewModel,
            )
        }

        composable("profile") {
            googleAuthUiClient.getSignedInUser()?.let { it1 ->
                ProfileScreen(
                    viewModel = signInViewModel,
                    userData = it1,
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
        }

        composable("sign_up") {
            SignUpScreen(
                viewModel = signInViewModel,
                onCreateAnAccountClick = {
                    navController.navigate("registration")
                },
                onHaveAnAccountClick = {
                    navController.navigate("sign_in")
                }
            )
        }

        composable("sign_in_2") {
            SignInScreen2(
                onForgetPasswordClick = { },
                onDontHaveAnAccountClick = {
                    navController.navigate("sign_up")
                    signInViewModel.resetPassword()
                },
                viewModel = signInViewModel,
                onEditClick = { navController.navigate("sign_in") },
                onLoginSuccess = { navController.navigate("my_app") },
            )
        }

        composable("registration") {
            RegistrationScreen(
                userInformationViewModel = userManagementViewModel,
                onContinueClick = {
                    userManagementViewModel.transportUserInformation(
                        signInViewModel.userData.value
                    )
                    userManagementViewModel.addUser()
                    navController.navigate("my_app")
                },
                viewModel = signInViewModel,
            )
        }

        composable("my_app") {
            val innerNavController = rememberNavController()

            MyScaffold(
                onProfileClick = { innerNavController.navigate("profile_screen") },
                onFriendsClick = { /* obsługa kliknięcia */ },
                onMyWorkoutsClick = { /* obsługa kliknięcia */ },
                onAIChatBotClick = { /* obsługa kliknięcia */ },
                onMessageClick = { /* obsługa kliknięcia */ },
                onAboutClick = { innerNavController.navigate("about_screen") },
                onLogoutClick = { /* obsługa wylogowania */ },
                onImageClick = { /* obsługa kliknięcia obrazka */ },
                onSearchClick = { /* obsługa wyszukiwania */ },
                userManagementViewModel = userManagementViewModel,
            ) { innerPadding ->
                NavHost(
                    navController = innerNavController,
                    startDestination = "about_screen", // np. ekran główny
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("profile_screen") {
                            com.example.gymbuddy.scaffoldscreens.ProfileScreen(
                                onImageClick = { },
                                onMoreClick = {},
                                onEditClick = {},
                                userManagementViewModel = userManagementViewModel,
                            )
                    }
                    composable("about_screen") {
                        AboutScreen()
                    }
                }
            }
        }

    }
}
