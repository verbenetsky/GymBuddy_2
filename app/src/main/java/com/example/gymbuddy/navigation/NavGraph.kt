package com.example.gymbuddy.navigation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymbuddy.ui.auth.SignInScreen
import com.example.gymbuddy.ui.auth.SignInScreen2
import com.example.gymbuddy.ui.auth.AuthViewModel
import com.example.gymbuddy.ui.auth.SignUpScreen
import com.example.gymbuddy.ui.profile.UserManagementViewModel
import com.example.gymbuddy.ui.auth.ProfileSetupScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymbuddy.ui.chatbot.ChatBotViewModel
import com.example.gymbuddy.ui.messages.ChatScreen
import com.example.gymbuddy.ui.messages.ChatViewModel
import com.example.gymbuddy.ui.search.UserSearchViewModel
import com.example.gymbuddy.ui.friends.FriendRequestViewModel
import com.example.gymbuddy.ui.common.AboutScreen
import com.example.gymbuddy.ui.chatbot.ChatBotScreen
import com.example.gymbuddy.ui.profile.GuestProfileScreen
import com.example.gymbuddy.ui.messages.MessagesScreen
import com.example.gymbuddy.ui.friends.MyFriendsScreen
import com.example.gymbuddy.ui.common.MyScaffold
import com.example.gymbuddy.ui.workout.MyWorkoutsScreen
import com.example.gymbuddy.ui.profile.ProfileScreen
import com.example.gymbuddy.ui.search.SearchScreen
import com.example.gymbuddy.ui.workout.AddWorkoutScreen
import com.example.gymbuddy.ui.workout.EditWorkoutScreen
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavGraph(
    navController: NavHostController,
    signInViewModel: AuthViewModel,
    userManagementViewModel: UserManagementViewModel,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel,
    userSearchViewModel: UserSearchViewModel,
) {

    val authState by signInViewModel.authState.collectAsState()

    if (authState is AuthViewModel.AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthViewModel.AuthState.Authenticated) "my_app" else "sign_in"
    ) {

        composable("sign_in") {

            SignInScreen(
                authViewModel = signInViewModel,
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
                authViewModel = signInViewModel,
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
                authViewModel = signInViewModel,
            )
        }

        composable("registration") {
            ProfileSetupScreen(
                userManagementViewModel = userManagementViewModel,
                authViewModel = signInViewModel,
                navigateToMyApp = { navController.navigate("my_app") },
            )
        }

        composable("my_app") {
            val context = LocalContext.current

            val chatBotViewModel: ChatBotViewModel = hiltViewModel()
            val scope = rememberCoroutineScope()

            val currentChatName by chatViewModel.currentChatName.collectAsState()
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
                onDeleteConversationClick = {
                    //ugly
                    scope.launch {
                        chatBotViewModel.deleteConversation {
                            Toast.makeText(
                                context,
                                "Konwersacja z AI została usunięta",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = innerNavController,
                    startDestination = "about_screen",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("about_screen") {
                        AboutScreen()
                    }

                    composable("profile_screen") {
                        ProfileScreen(
                            signInViewModel = signInViewModel,
                            userManagementViewModel = userManagementViewModel,
                            onDeleteClick = { navController.navigate("sign_in") },
                        )
                    }

                    // delete workouts WorkoutViewModel
                    // delete ai chatBot conversation ChatBotViewModel
                    // delete channels ChannelViewModel
                    // usunac wszystkie friendsShip z db (tam gdzie accepted)

                    // usunac wszystkie messages ktore sa przypisane do czatu w ktorym jest user ktorego usuwamy, czyli:
                    // pobieramy liste wszystkich channels id, w ktorych jest nasz user ktorego chcemy usunac
                    // usuwamy wyszsktie te wiadomosci gdzie w wiedomosci w channelId jest jeden z naszych id

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
                            userManagementViewModel = userManagementViewModel,
                            innerNavController = innerNavController
                        )
                    }

                    composable("my_friends_screen") {
                        MyFriendsScreen(
                            friendRequestViewModel = friendRequestViewModel,
                            userSearchViewModel = userSearchViewModel,
                            onSeeProfileClick = { innerNavController.navigate("guess_profile_screen") },
                            innerNavController = innerNavController,
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
                        route = "chat/{channelId}",
                        arguments = listOf(
                            navArgument("channelId") { type = NavType.StringType },
                        )
                    )
                    { backStackEntry ->
                        val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                        ChatScreen(
                            userManagementViewModel = userManagementViewModel,
                            channelID = channelId,
                            innerNavController = innerNavController,
                            userSearchViewModel = userSearchViewModel,
                            chatViewModel = chatViewModel
                        )
                    }

                    composable(route = "chatBot_screen") {
                        ChatBotScreen(chatBotViewModel = chatBotViewModel)
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
