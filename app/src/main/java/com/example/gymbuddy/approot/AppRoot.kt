package com.example.gymbuddy.approot

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymbuddy.data.authentication.SignInViewModel
import com.example.gymbuddy.data.authentication.SignInScreen
import com.example.gymbuddy.data.authentication.UserManagementViewModel
import com.example.gymbuddy.data.authentication.GoogleAuthUiClient
import androidx.lifecycle.LifecycleCoroutineScope
import android.content.Context
import com.example.gymbuddy.scaffoldscreens.AboutScreen
import com.example.gymbuddy.scaffoldscreens.MyScaffold

@Composable
fun AppRoot(
    authState: SignInViewModel.AuthState,
    navController: NavHostController,
    signInViewModel: SignInViewModel,
    userManagementViewModel: UserManagementViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context
) {
    if (authState == SignInViewModel.AuthState.Authenticated ||
        authState == SignInViewModel.AuthState.GoogleAuthenticated) {
        // Wyświetlamy główną część aplikacji (z Scaffoldem)
        MyScaffold(
            onProfileClick = { /* obsługa */ },
            onFriendsClick = { /* obsługa */ },
            onMyWorkoutsClick = { /* obsługa */ },
            onAIChatBotClick = { /* obsługa */ },
            onMessageClick = { /* obsługa */ },
            onAboutClick = { navController.navigate("about_screen") },
            onLogoutClick = {
                signInViewModel.signOut()
                navController.navigate("sign_in") {
                    popUpTo(0)
                }
            },
            onImageClick = { /* obsługa */ },
            onSearchClick = { /* obsługa */ },
            content = {
                AboutScreen()
            },
            userManagementViewModel = userManagementViewModel,
        )
    } else {
        // Wyświetlamy ekran logowania bez Scaffolda
        SignInScreen(
            state = signInViewModel.state.collectAsStateWithLifecycle().value,
            onSignInClick = { /* logika logowania */ },
            onSignUpClick = { navController.navigate("sign_up") },
            onContinueSignInScreenClick = {
                navController.navigate("sign_in_2")
                signInViewModel.resetPassword()
            },
            viewModel = signInViewModel
        )
    }
}

