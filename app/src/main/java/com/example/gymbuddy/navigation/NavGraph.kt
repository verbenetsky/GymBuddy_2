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
import com.example.gymbuddy.scaffoldscreens.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

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
//            LaunchedEffect(authState) {
//                if (authState == SignInViewModel.AuthState.Authenticated || authState == SignInViewModel.AuthState.GoogleAuthenticated) {
//                    navController.navigate("my_app")
//                } else {
//                    navController.navigate("sign_in")
//                }
//            }

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

//        composable("profile") {
//            googleAuthUiClient.getSignedInUser()?.let { it1 ->
//                ProfileScreen(
//                    viewModel = signInViewModel,
//                    userData = it1,
//                    onSignOut = {
//                        signInViewModel.clearLoginForm()
//                        if (googleAuthUiClient.getSignedInUser() != null) {
//                            lifecycleScope.launch {
//                                googleAuthUiClient.signOut()
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Signed out",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                            signInViewModel.signOut()
//                            navController.navigate("sign_in")
//                        } else {
//                            signInViewModel.signOut()
//                        }
//                    }
//                )
//            }
//        }

        composable("sign_in_2") {
            SignInScreen2(
                onForgetPasswordClick = { },
                onDontHaveAnAccountClick = {
                    navController.navigate("sign_up")
                    signInViewModel.resetPassword()
                },
                viewModel = signInViewModel,
                onEditClick = { navController.navigate("sign_in") },
                onLoginSuccess = {
                    navController.navigate("my_app")
                    userManagementViewModel.getUserFromFireStoreToViewModel()
                },
            )
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


        composable("registration") {
            RegistrationScreen(
                userInformationViewModel = userManagementViewModel,
                onContinueClick = {
                    userManagementViewModel.transportUserInformation(
                        signInViewModel.userData.value
                    )
                    userManagementViewModel.addUser()
                    navController.navigate("my_app")
                    signInViewModel.updateAuthStateToAuthenticated()
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
                onLogoutClick = {
                    signInViewModel.logOut()
                    navController.navigate("sign_in")
                    userManagementViewModel.clearForm()
                    signInViewModel.clearUserData()
                },
                onImageClick = { /* obsługa kliknięcia obrazka */ },
                onSearchClick = { /* obsługa wyszukiwania */ },
                userManagementViewModel = userManagementViewModel,
            ) { innerPadding ->
                NavHost(
                    navController = innerNavController,
                    startDestination = "about_screen",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("profile_screen") {
                        ProfileScreen(
                            onImageClick = {},
                            onDeleteClick = {
                                navController.navigate("sign_in")
                                signInViewModel.updateAuthStateToLoading()
                                signInViewModel.deleteUser()
                                signInViewModel.clearUserData()
                                signInViewModel.updateAuthStateToUnauthenticated()
                                userManagementViewModel.deleteUserDataFromFirestore()
                                userManagementViewModel.clearForm()
                            },
                            authState = authState,
                            userManagementViewModel = userManagementViewModel,
                            onSaveClick = {
                                userManagementViewModel.updateUser(
                                    userManagementViewModel.userInformationState.value
                                )
                            },
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
