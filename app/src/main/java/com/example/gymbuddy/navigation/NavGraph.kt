package com.example.gymbuddy.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymbuddy.data.authentication.SignInScreen
import com.example.gymbuddy.data.authentication.SignInScreen2
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.SignUpScreen
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.presentation.RegistrationNewUserScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymbuddy.chat.ChatScreen
import com.example.gymbuddy.chat.ChatViewModel
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

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavGraph(
    authState: SignInViewModel.AuthState,
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    userManagementViewModel: UserManagementViewModel,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel,
    userSearchViewModel: UserSearchViewModel,
) {
    NavHost(navController = navController, startDestination = "sign_in") {

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
                onSignUpClick = { navController.navigate("sign_up") },
                onContinueSignInScreenClick = { navController.navigate("sign_in_2") },
                clearUserInformation = { userManagementViewModel.clearForm() },
                navigateToMyApp = { navController.navigate("my_app") },
                navigateToRegistration = {
                    navController.navigate("registration") {
                        popUpTo("sign_in") {
                            inclusive = true
                        }
                    }
                },
                userManagementViewModel = userManagementViewModel
            )
        }

        composable("sign_in_2") {
            SignInScreen2(
                signInViewModel = signInViewModel,
                onDontHaveAnAccountClick = { navController.navigate("sign_up") },
                onEditClick = { navController.navigate("sign_in") },
                navigateToMyApp = { navController.navigate("my_app") },
                userManagementViewModel = userManagementViewModel,
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

            val currentChatName by chatViewModel.currentChatName.collectAsState(initial = "")
            val title = currentChatName.ifBlank { "GymBuddy" }
            val innerNavController = rememberNavController()
            // Twoje poprzednie podejście (deklaracja w mainie) nie działało, bo innerNavController
            // miał inny zakres życia niż NavHost i nie zdążył dostać swojego ViewModelStore.

            MyScaffold(
                onFriendsClick = { innerNavController.navigate("my_friends_screen") },
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
                determineName = { title },
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
                        SearchScreen(
                            onProfileClick = { innerNavController.navigate("profile_screen") },
                            onGuestProfileClick = { innerNavController.navigate("guess_profile_screen") },
                            userSearchViewModel = userSearchViewModel,
                            userManagementViewModel = userManagementViewModel
                        )
                    }

                    composable("guess_profile_screen") {
                        GuestProfileScreen(
                            authState = authState,
                            userSearchViewModel = userSearchViewModel,
                            friendRequestViewModel = friendRequestViewModel,
                            onSendMessageClick = { }, //todo
                            userManagementViewModel = userManagementViewModel
                        )
                    }

                    composable("my_friends_screen") {
                        MyFriendsScreen(
                            friendRequestViewModel = friendRequestViewModel,
                            userSearchViewModel = userSearchViewModel,
                            onSeeProfileClick = { innerNavController.navigate("guess_profile_screen") },
                        )
                    }

                    composable(route = "message_screen") {
                        MessagesScreen(
                            innerNavController = innerNavController,
                            friendRequestViewModel = friendRequestViewModel,
                            chatViewModel = chatViewModel,
                            userSearchViewModel = userSearchViewModel
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
                            userSearchViewModel = userSearchViewModel,
                            chatViewModel = chatViewModel
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
                            friendRequestViewModel = friendRequestViewModel
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
