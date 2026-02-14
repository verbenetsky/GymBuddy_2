package com.example.gymbuddy.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import com.example.gymbuddy.data.model.SignInResultCred
import com.example.gymbuddy.ui.profile.UserManagementViewModel
import com.example.gymbuddy.ui.common.CommonUtils

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    userManagementViewModel: UserManagementViewModel,
    onContinueSignInScreenClick: () -> Unit,
    navigateToRegistration: () -> Unit,
    navigateToMyApp: () -> Unit,
    clearUserInformation: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    val userData by authViewModel.userData.collectAsState()
    val validation by authViewModel.signInValidation.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val activity = context as Activity
    val accountManager = remember { CredentialManager(activity) }

    var triedAuto by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        keyboardController?.hide()
        authViewModel.resetPassword()
        clearUserInformation()
    }

    LaunchedEffect(Unit) {
        if (!triedAuto) {
            triedAuto = true
            when (val res = accountManager.signIn()) {
                is SignInResultCred.Password -> {
                    authViewModel.logIn(res.email, res.password, onSuccess = {
                        userManagementViewModel.fetchUserData()
                        authViewModel.setAuthState(AuthViewModel.AuthState.Authenticated)
                        navigateToMyApp()
                        Toast.makeText(
                            context,
                            "Log in Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, onError = {
                        Toast.makeText(context, "Invalid email and/or password.", Toast.LENGTH_SHORT).show()
                    })
                }

                is SignInResultCred.Google -> {
                    authViewModel.authenticateWithGoogle(res.idToken, onSuccess = { isNew, uid ->
                        if (isNew) {
                            println("new user")
                            authViewModel.setAuthState(AuthViewModel.AuthState.Authenticated)
                            navigateToRegistration()
                            authViewModel.setUserData(res.email, uid)
                        } else {
                            println("old user")
                            authViewModel.setAuthState(AuthViewModel.AuthState.Authenticated)
                            userManagementViewModel.fetchUserData()
                            navigateToMyApp()
                            Toast.makeText(
                                context,
                                "Log in Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }, onError = {

                    })
                }

                SignInResultCred.NoCredentials -> {
                    Toast.makeText(context, "Brak zapisanych danych", Toast.LENGTH_SHORT).show()
                }

                SignInResultCred.Cancelled -> {
//                Toast.makeText(context, "Anulowano", Toast.LENGTH_SHORT).show()
                }

                SignInResultCred.Failure -> {
                    Toast.makeText(context, "Błąd odczytu danych", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background( if (isSystemInDarkTheme()) Color.Black else Color.White)
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box {
            Image(
                painter = painterResource(id = CommonUtils.logoTheme()),
                contentDescription = null,
                modifier = Modifier.size(185.dp)
            )
        }

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userData.email,
            onValueChange = { authViewModel.updateEmail(it) },
            label = { Text("Enter your email") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
            },
            isError = !validation.isEmailValid && userData.email.isNotEmpty(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        if (!validation.isEmailValid && userData.email.isNotEmpty()) {
            Text(
                text = stringResource(R.string.invalid_email),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onContinueSignInScreenClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = validation.isEmailValid && userData.email.isNotEmpty()
        ) {
            Text(
                text = stringResource(R.string.continue_text),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.dont_have_an_account),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable { onSignUpClick() }
        )

        Spacer(modifier = Modifier.height(100.dp))
    }

}
