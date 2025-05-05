package com.example.gymbuddy.data.authentication

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymbuddy.R
import com.example.gymbuddy.utils.CommonUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SignUpScreen(
    signInViewModel: SignInViewModel,
    navigateToRegistration: () -> Unit,
    onHaveAnAccountClick: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val userData by signInViewModel.userData.collectAsState()
    val password by signInViewModel.password.collectAsState()
    val validation by signInViewModel.signInValidation.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val authState by signInViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        signInViewModel.clearLoginForm()
        keyboardController?.hide()
        signInViewModel.resetPassword()
    }

    if (authState == SignInViewModel.AuthState.Loading) {
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
            modifier = Modifier
                .fillMaxSize()
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = CommonUtils.logoTheme()),
                contentDescription = null,
                modifier = Modifier.size(185.dp)
            )

            Text(
                text = stringResource(R.string.create_an_account),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userData.email,
                singleLine = true,
                onValueChange = {
                    if (it.length < 30) {
                        signInViewModel.updateEmail(it)
                    }
                },
                label = { Text("Email address") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length < 30) {
                        signInViewModel.updatePassword(it)
                    }
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "password Icon")
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible })
                    {
                        Icon(imageVector = icon, contentDescription = "show/hide password")
                    }

                }
            )

            if (!validation.isPasswordValid && password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.password_requirements),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(R.string.password_length),
                    color = if (signInViewModel.analyzePasswordRequirementsLength(password)) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(R.string.password_one_digit),
                    color = if (signInViewModel.analyzePasswordRequirementsOneDigit(password)) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    signInViewModel.signUp(
                        userData.email,
                        password,
                        onSuccess = {
                            navigateToRegistration()
                            Toast.makeText(context, "Account Created", Toast.LENGTH_LONG).show()
                            signInViewModel.setUserData(userData.email, Firebase.auth.currentUser!!.uid)
                            signInViewModel.resetPassword()
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        onCanceled = {
                            navigateToRegistration()
                            signInViewModel.resetPassword()
                        },
                        onFailure = {
                            println("failure")
                        }
                    )
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = validation.isEmailValid &&
                        validation.isPasswordValid &&
                        password.isNotEmpty() &&
                        userData.email.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.create_an_account),
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Text(
                stringResource(R.string.already_have_an_account),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { onHaveAnAccountClick() }
            )
        }
    }
}
