package com.example.gymbuddy.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymbuddy.data.authentication.SignInScreen
import com.example.gymbuddy.data.authentication.SignInScreen2
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.SignUpScreen
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.presentation.RegistrationNewUserScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
import kotlin.math.sin

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavGraph(
    authState: SignInViewModel.AuthState,
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    buttonStateManager: ButtonStateManager,
    userManagementViewModel: UserManagementViewModel,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    userSearchViewModel: UserSearchViewModel,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context
) {
    NavHost(navController = navController, startDestination = "sign_in") {
        val currentUser = Firebase.auth.currentUser?.uid ?: "non"

        composable("sign_in") {

            LaunchedEffect(authState) {
                if (authState == SignInViewModel.AuthState.Authenticated ||
                    authState == SignInViewModel.AuthState.GoogleAuthenticated
                ) {
                    if (navController.currentDestination?.route != "my_app") { // my_app
                        navController.navigate("my_app")
                    }
                } else {
                    if (navController.currentDestination?.route != "sign_in") { // sign_in
                        navController.navigate("sign_in")
                    }
                }
            }

            SignInScreen(
                signInViewModel = signInViewModel,

                onSignInGoogleButtonClick = {
                    // todo
                },
                onSignUpClick = { navController.navigate("sign_up") },

                onContinueSignInScreenClick = { navController.navigate("sign_in_2") },

                clearUserInformation = { userManagementViewModel.clearForm() }
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
                signInViewModel = signInViewModel,
                onDontHaveAnAccountClick = {
                    navController.navigate("sign_up")
                },
                onEditClick = { navController.navigate("sign_in") },
//                onLogInClick = {
//                    signInViewModel.logIn(
//                        userData.email, password,
//                        onSuccess = {
//                            navController.navigate("my_app")
//                            userManagementViewModel.getUserFromFireStoreToViewModel()
//                        },
//                        onError = {
//                            Toast.makeText(
//                                applicationContext,
//                                "Wrong email or password",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    )
//                }
                navigateToMyApp = { navController.navigate("my_app") },
                userManagementViewModel = userManagementViewModel,
//                onLoginSuccess = {
//                    navController.navigate("my_app")
//                    userManagementViewModel.getUserFromFireStoreToViewModel()
//                },
            )
        }

        composable("sign_up") {
            SignUpScreen(
                onHaveAnAccountClick = { navController.navigate("sign_in") },
                navigateToRegistration = { navController.navigate("registration") },
                signInViewModel = signInViewModel,
            )
        }


        composable("registration") {
            RegistrationNewUserScreen(
                userManagementViewModel = userManagementViewModel,
                signInViewModel = signInViewModel,
                navigateToMyApp = { navController.navigate("my_app") },
            )
        }

        composable("my_app") {
            val context = LocalContext.current // todo delete
            val innerNavController = rememberNavController()
            // Twoje poprzednie podejście (deklaracja w mainie) nie działało, bo innerNavController
            // miał inny zakres życia niż NavHost i nie zdążył dostać swojego ViewModelStore.


            MyScaffold(
                onFriendsClick = {
                    friendRequestViewModel.fetchAllFriendRequestsAndFullInformation(currentUser)
                    friendRequestViewModel.getAllFriend(currentUser)
                    innerNavController.navigate("my_friends_screen")
                },

                onProfileClick = { innerNavController.navigate("profile_screen") },

                onMyWorkoutsClick = { innerNavController.navigate("my_workouts_screen") },

                onAIChatBotClick = { innerNavController.navigate("chatBot_screen") },

                onMessageClick = { innerNavController.navigate("message_screen") },

                onBackArrowClick = { innerNavController.navigate("profile_screen") },

                onAboutClick = { innerNavController.navigate("about_screen") },

                navigateToSingInScreen = { navController.navigate("sign_in") },

                onSearchClick = { innerNavController.navigate("search_screen") },

                signInViewModel = signInViewModel,

                userManagementViewModel = userManagementViewModel,

                innerNavController = innerNavController,

            ) { innerPadding ->
                NavHost(
                    navController = innerNavController,
                    startDestination = "about_screen",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("profile_screen") {
                        ProfileScreen(
                            signInViewModel = signInViewModel,

                            userManagementViewModel = userManagementViewModel,

                            onDeleteClick = { navController.navigate("sign_in") }
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
                            userManagementViewModel = userManagementViewModel,
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
