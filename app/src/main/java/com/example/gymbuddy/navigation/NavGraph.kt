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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymbuddy.buttonState.ButtonStateManager
import com.example.gymbuddy.chat.ChatScreen
import com.example.gymbuddy.data.authentication.UserSearchViewModel
import com.example.gymbuddy.pushnotification.FriendRequestViewModel
import com.example.gymbuddy.scaffoldscreens.AboutScreen
import com.example.gymbuddy.scaffoldscreens.ChatBotScreen
import com.example.gymbuddy.scaffoldscreens.GuestProfileScreen
import com.example.gymbuddy.scaffoldscreens.MessagesScreen
import com.example.gymbuddy.scaffoldscreens.MyFriendsScreen
import com.example.gymbuddy.scaffoldscreens.MyScaffold
import com.example.gymbuddy.scaffoldscreens.MyWorkoutsScreen
import com.example.gymbuddy.scaffoldscreens.ProfileScreen
import com.example.gymbuddy.scaffoldscreens.SearchScreen
import com.example.gymbuddy.workout.AddWorkoutScreen
import com.example.gymbuddy.workout.EditWorkoutScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun NavGraph(
    authState: SignInViewModel.AuthState,
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    buttonStateManager: ButtonStateManager,
    userManagementViewModel: UserManagementViewModel,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    userSearchViewModel: UserSearchViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context
) {
    NavHost(navController = navController, startDestination = "sign_in") {
        val currentUser = Firebase.auth.currentUser?.uid ?: "non"

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
            val password by signInViewModel.password.collectAsState()
            val userData by signInViewModel.userData.collectAsState()

            SignInScreen2(
                onForgetPasswordClick = { },
                onDontHaveAnAccountClick = {
                    navController.navigate("sign_up")
                    signInViewModel.resetPassword()
                },
                viewModel = signInViewModel,
                userManagementViewModel = userManagementViewModel,
                friendRequestViewModel = friendRequestViewModel,
                onEditClick = { navController.navigate("sign_in") },
                onLogInClick = {
                    signInViewModel.signIn(
                        userData.email, password,
                        onSuccess = {
                            navController.navigate("my_app")
                            userManagementViewModel.getUserFromFireStoreToViewModel()
                        },
                        onError = {
                            Toast.makeText(
                                applicationContext,
                                "Wrong email or password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                },
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
                onFriendsClick = {
                    friendRequestViewModel.fetchAllFriendRequestsAndFullInformation(currentUser)
                    friendRequestViewModel.getAllFriend(currentUser)
                    innerNavController.navigate("my_friends_screen")
                },
                onMyWorkoutsClick = { innerNavController.navigate("my_workouts_screen") },
                onAIChatBotClick = { innerNavController.navigate("chatBot_screen") },
                onMessageClick = { innerNavController.navigate("message_screen") },
                onBackArrowClick = {
                    innerNavController.navigate("profile_screen")
                },
                onAboutClick = { innerNavController.navigate("about_screen") },
                onLogoutClick = {
                    signInViewModel.logOut()
                    navController.navigate("sign_in")
                    userManagementViewModel.clearForm()
                    signInViewModel.clearUserData()
                },

                innerNavController = innerNavController,
                onSearchClick = {
                    innerNavController.navigate("search_screen")
                },
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
                            onConfirmChangeImageClick = {
                                userManagementViewModel.deleteProfilePicture(userManagementViewModel.userInformationState.value.profilePictureUrl)
                                userManagementViewModel.updateProfilePictureToDefault()
                                userManagementViewModel.updateUser(userManagementViewModel.userInformationState.value)
                            },
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
                    composable("search_screen") {
                        val userSearchState = userSearchViewModel.userSearchState.collectAsState()
                        val userFoundInformation =
                            userSearchViewModel.userFoundInformation.collectAsState()
                        SearchScreen(
                            userSearchState = userSearchState.value,
                            onSearchClick = {
                                userSearchViewModel.searchUser(
                                    username = userSearchViewModel.searchQuery.value,
                                    onSuccessSearch = {
                                        userSearchViewModel.updateSearchState(UserSearchViewModel.UserSearchState.FoundUser)
                                        friendRequestViewModel.determineButtonState(
                                            userSearchViewModel.userFoundInformation.value.userId,
                                            onSuccess = {

                                            }
                                        )
                                    },
                                    onFailureSearch = {
                                        userSearchViewModel.updateSearchState(UserSearchViewModel.UserSearchState.NothingFound)
                                    }
                                )
                            },
                            onProfileClick = { innerNavController.navigate("profile_screen") },
                            onGuestProfileClick = { innerNavController.navigate("guess_profile_screen") },
                            onSendRequestClick = {
                                friendRequestViewModel.transferDataFromFoundUserToFriendRequest(
                                    userSearchViewModel.userFoundInformation.value
                                )
                                friendRequestViewModel.sendFriendRequestToUser()
                                lifecycleScope.launch {
                                    friendRequestViewModel.addFriendRequestToDatabase(
                                        onSuccess = {
                                            Toast.makeText(
                                                applicationContext,
                                                "Request has been sent.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                applicationContext,
                                                "Something went wrong.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                            buttonStateManager = buttonStateManager,
                            userSearchViewModel = userSearchViewModel,
                            onDeclineClick = {
                                friendRequestViewModel.deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
                                    currentUserId = currentUser,
                                    friendId = userFoundInformation.value.userId,
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },

                                    )
                                userSearchViewModel.searchUser(
                                    username = userSearchViewModel.searchQuery.value,
                                    onSuccessSearch = {
                                        userSearchViewModel.updateSearchState(UserSearchViewModel.UserSearchState.FoundUser)
                                        friendRequestViewModel.determineButtonState(
                                            userSearchViewModel.userFoundInformation.value.userId,
                                            onSuccess = {

                                            }
                                        )
                                    },
                                    onFailureSearch = {
                                        userSearchViewModel.updateSearchState(UserSearchViewModel.UserSearchState.NothingFound)
                                    }
                                )
                            },
                            onRemoveClick = {
                                friendRequestViewModel.deleteFriend(
                                    currentUser,
                                    userFoundInformation.value.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have removed a friend.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                userSearchViewModel.clearUserFoundInformation()
                                userSearchViewModel.updateSearchState(UserSearchViewModel.UserSearchState.NothingFound)
                            }
                        )
                    }
                    composable("guess_profile_screen") {
                        val userFoundInformation by userSearchViewModel.userFoundInformation.collectAsState()
                        GuestProfileScreen(
                            authState = authState,
                            userSearchViewModel = userSearchViewModel,
                            friendRequestViewModel = friendRequestViewModel,
                            onSendMessageClick = {
                                Toast.makeText(
                                    applicationContext,
                                    "Only friends can send messages.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },

                            onRemoveFriendClick = {
                                friendRequestViewModel.deleteFriend(
                                    currentUser,
                                    userFoundInformation.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have removed a friend.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                innerNavController.navigate("profile_screen")
                            },
                            onDeclineClick = {
                                friendRequestViewModel.deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
                                    currentUserId = currentUser,
                                    friendId = userFoundInformation.userId,
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                innerNavController.navigate("profile_screen")
                            },
                            onAcceptClick = {
                                friendRequestViewModel.addFriendsInformationToDatabase(
                                    currentUserId = Firebase.auth.currentUser!!.uid,
                                    friendId = userFoundInformation.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have got a new friend!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        innerNavController.navigate("my_friends_screen")
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                friendRequestViewModel.deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
                                    currentUserId = Firebase.auth.currentUser!!.uid,
                                    friendId = userFoundInformation.userId,
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                friendRequestViewModel.getAllFriend(currentUser)
                            },
                            onSendFriendRequestClick = {
                                friendRequestViewModel.transferDataFromFoundUserToFriendRequest(
                                    userSearchViewModel.userFoundInformation.value
                                )
                                friendRequestViewModel.sendFriendRequestToUser()
                                lifecycleScope.launch {
                                    friendRequestViewModel.addFriendRequestToDatabase(
                                        onSuccess = {
                                            Toast.makeText(
                                                applicationContext,
                                                "Request has been sent.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                applicationContext,
                                                "Something went wrong.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        )
                    }

                    composable("my_friends_screen") {
                        MyFriendsScreen(
                            friendRequestViewModel = friendRequestViewModel,
                            onAcceptClick = {
                                friendRequestViewModel.addFriendsInformationToDatabase(
                                    currentUserId = Firebase.auth.currentUser!!.uid,
                                    friendId = it.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have got a new friend!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                friendRequestViewModel.deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
                                    currentUserId = Firebase.auth.currentUser!!.uid,
                                    friendId = it.userId,
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                                friendRequestViewModel.getAllFriend(currentUser)
                            },
                            onSeeProfileClick = { userId ->
                                userSearchViewModel.getUserBasedOnUserId(userId,
                                    onSuccess = {
                                        friendRequestViewModel.determineButtonState(
                                            userId,
                                            onSuccess = {
                                                innerNavController.navigate("guess_profile_screen")
                                            })
                                    })
                            },
                            onDeclineClick = {
                                friendRequestViewModel.deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
                                    currentUserId = Firebase.auth.currentUser!!.uid,
                                    friendId = it.userId,
                                    onFailure = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Something gone wrong, try again!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },

                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have declined friend request.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                            },
                            onRemoveFriendClick = { friend ->
                                friendRequestViewModel.deleteFriend(
                                    currentUser,
                                    friend.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            applicationContext,
                                            "You have removed a friend.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                                friendRequestViewModel.getAllFriend(currentUser)
                            },
                        )
                    }
                    composable(route = "message_screen") {
                        MessagesScreen(
                            innerNavController = innerNavController,
                        )
                    }

                    composable(
                        route = "chat/{channelId}/{userId}",
                        arguments = listOf(
                            navArgument("channelId") { type = NavType.StringType },
                            navArgument("userId") { type = NavType.StringType }
                        )
                    )
                    { backStackEntry ->
                        val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                        val userId = backStackEntry.arguments?.getString("userId")
                        ChatScreen(
                            userManagementViewModel = userManagementViewModel,
                            channelID = channelId,
                            userId = userId,
                            innerNavController = innerNavController,
                        )
                    }
                    composable(route = "chatBot_screen") {
                        ChatBotScreen()
                    }
                    composable(route = "my_workouts_screen") {
                        MyWorkoutsScreen(
                            navigateToAddWorkoutScreen = { innerNavController.navigate("add_workout_screen") },
                            innerNavController = innerNavController,
                            navigateToEditScreen = { innerNavController.navigate("edit_workout_screen") },
                        )
                    }
                    composable(route = "add_workout_screen") {
                        AddWorkoutScreen(
                            navigateToMyWorkoutsScreen = { innerNavController.navigate("my_workouts_screen") },
                            innerNavController = innerNavController,
                        )
                    }
                    composable(route = "edit_workout_screen") {
                        EditWorkoutScreen(
                            navigateToMyWorkoutsScreen = { innerNavController.navigate("my_workouts_screen") },
                            innerNavController = innerNavController,
                        )
                    }
                }
            }
        }
    }
}
