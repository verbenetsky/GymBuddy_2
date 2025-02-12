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
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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

            LaunchedEffect(authState) {
                if (authState == SignInViewModel.AuthState.Authenticated ||
                    authState == SignInViewModel.AuthState.GoogleAuthenticated
                ) {
                    if (navController.currentDestination?.route != "my_app") {
                        navController.navigate("my_app")
                    }
                } else {
                    if (navController.currentDestination?.route != "sign_in") {
                        navController.navigate("sign_in")
                    }
                }
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
                    signInViewModel.updateAuthState(SignInViewModel.AuthState.AuthenticatedButNotRegister)
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
                    userManagementViewModel.addUsernameToDataBase(
                        username = userManagementViewModel.userInformationState.value.username,
                        onSuccessfulUsernameAddition = {
                            Toast.makeText(
                                applicationContext,
                                "Information successfully added",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("my_app")
                            userManagementViewModel.transportUserInformation(
                                signInViewModel.userData.value
                            )
                            userManagementViewModel.addUser()
                            signInViewModel.updateAuthState(SignInViewModel.AuthState.Authenticated)
                        },
                        onFailedUsernameAddition = {
                            Toast.makeText(
                                applicationContext,
                                "Username is already taken, try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onEmptyUsername = {
                            Toast.makeText(
                                applicationContext,
                                "Username cannot be empty.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    )
                },
                viewModel = signInViewModel,
            )
        }

        composable("my_app") {
            val innerNavController = rememberNavController()
            val context = LocalContext.current

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
                            onDeleteClick = {
                                signInViewModel.updateAuthState(SignInViewModel.AuthState.Loading)
                                navController.navigate("sign_in")
                                userManagementViewModel.deleteUsernameFromDataBase(
                                    userManagementViewModel.userInformationState.value.username
                                )
                                signInViewModel.deleteUser()
                                signInViewModel.clearUserData()
                                userManagementViewModel.deleteUserDataFromFirestore()
                                userManagementViewModel.clearForm()
                                signInViewModel.updateAuthState(SignInViewModel.AuthState.Unauthenticated)
                            },
                            authState = authState,
                            userManagementViewModel = userManagementViewModel,
                            onSaveClick = {
                                userManagementViewModel.addUsernameToDataBase(
                                    userManagementViewModel.bufferUserName.value,
                                    onSuccessfulUsernameAddition = {
                                        userManagementViewModel.updateUsername(
                                            userManagementViewModel.bufferUserName.value
                                        )
                                        userManagementViewModel.updateUser(userManagementViewModel.userInformationState.value)
                                        userManagementViewModel.uploadProfilePicture(
                                            userManagementViewModel.userInformationState.value.profilePictureUrl.toUri()
                                        )
                                        Toast.makeText(
                                            context,
                                            "Username successfully updated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailedUsernameAddition = {
                                        Toast.makeText(
                                            context,
                                            "Username already taken",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        userManagementViewModel.updateToOldUsername()
                                    },
                                    onEmptyUsername = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Username cannot be empty.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
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
