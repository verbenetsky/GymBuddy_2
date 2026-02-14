package com.example.gymbuddy.ui.auth

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.gymbuddy.ui.profile.UserManagementViewModel
import com.example.gymbuddy.ui.common.rememberImeState

@Composable
fun SignInScreen2(
    authViewModel    : AuthViewModel,
    userManagementViewModel: UserManagementViewModel,
    navigateToMyApp: () -> Unit,
    onDontHaveAnAccountClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }

    val authState  by authViewModel.authState.collectAsState()
    val userData   by authViewModel.userData.collectAsState()
    val validation by authViewModel.signInValidation.collectAsState()
    val password   by authViewModel.password.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current


    var forgotPasswordDialogState by remember { mutableStateOf(false) }

    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        keyboardController?.hide()
        authViewModel.resetPassword()
    }

    LaunchedEffect(imeState.value) {
        if(imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    if (authState == AuthViewModel.AuthState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background( if (isSystemInDarkTheme()) Color.Black else Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(R.string.enter_your_password),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userData.email,
                onValueChange = {},
                label = { Text("Email address") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onEditClick() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Icon"
                        )
                    }
                },
                singleLine = true,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length < 30) {
                        authViewModel.updatePassword(it)
                    }
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val icon = if (passwordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Show/hide password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.forgot_password),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { forgotPasswordDialogState = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    authViewModel.logIn(
                        email = userData.email,
                        password = password,
                        onSuccess = {
                            authViewModel.setAuthState(AuthViewModel.AuthState.Authenticated)
                            navigateToMyApp()
                            userManagementViewModel.fetchUserData()
                            Toast.makeText(
                                context,
                                "Log in Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = { e ->
                            Toast.makeText(
                                context,
                                e,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = validation.isPasswordValid && password.isNotEmpty() && userData.email.isNotEmpty()
            ) {
                Text(
                    text = ("Log In"),
                    style = MaterialTheme.typography.displayMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.dont_have_an_account),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { onDontHaveAnAccountClick() }
            )
        }
    }

    if (forgotPasswordDialogState) {
        AlertDialogForgotPassword(
            changeDialogState = { newValue -> forgotPasswordDialogState = newValue },
            onSendResetEmailClick = {
                authViewModel.sendResetPasswordEmail(
                    email = it,
                    onSuccess = {
                        Toast.makeText(context, "Email send", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onError = { e ->
                        Toast.makeText(context, "Something gone wrong: $e", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            },
            email = userData.email
        )
    }
}



@Composable
fun AlertDialogForgotPassword(
    email: String,
    changeDialogState: (Boolean) -> Unit,
    onSendResetEmailClick: (String) -> Unit,
) {
    var inputEmail by remember { mutableStateOf(email) }


    AlertDialog(
        onDismissRequest = {
            changeDialogState(false)
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Forgot Password")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter your email address",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { inputEmail = it },
                    label = { Text("Enter email address") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSendResetEmailClick(inputEmail)
                    changeDialogState(false)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    changeDialogState(false)
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

