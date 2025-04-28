package com.example.gymbuddy.data.authentication

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymbuddy.R
import com.example.gymbuddy.ui.theme.Poppins
import com.example.gymbuddy.utils.CommonUtils
import com.example.gymbuddy.utils.CommonUtils.textGoogleButtonColor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()
    val activity = context as Activity
    val accountManager = remember { CredentialManager(activity) }

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
                .background(Color.Black)
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
                    keyboardController?.hide() // chowamy klawiature
                    signInViewModel.signUp(
                        userData.email,
                        password,
                        onSuccess = {
                            scope.launch {
                                val res = accountManager.signUp(userData.email, password)
                                when (res) {
                                    is SignUpResult.Success -> {
                                        navigateToRegistration()
                                        signInViewModel.setUserData(
                                            userData.email,
                                            Firebase.auth.currentUser!!.uid
                                        )
                                        Toast.makeText(
                                            context,
                                            "Account Created",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    SignUpResult.Cancelled -> {
                                        Toast.makeText(context, "Saving credentials canceled", Toast.LENGTH_SHORT).show()
                                    }

                                    SignUpResult.Failure -> {
                                        Toast.makeText(context, "Cannot save a login", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    )


//                    signInViewModel.signUp( // rejestracja
//                        userData.email,
//                        password,
//                        onSuccess = {
//                            navigateToRegistration()
//                            signInViewModel.setUserData(userData.email,Firebase.auth.currentUser!!.uid) // ustawiamy userData, czyli dodajemy email i userID
//                            Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show()
//                        },
//                        onError = { errorMessage ->
//                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
//                        }
//                    )
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp),
                shape = RoundedCornerShape(4.dp),
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

//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(18.dp))
//                    .clickable {
//                        scope.launch {
//                            when (val res = accountManager.silentSignIn(
//                                "359448700030-e6qcjkoqntc9vnics2vhse2u3nd3dtaa.apps.googleusercontent.com",
//                                interactive = true           // pełny picker
//                            )) {
//                                is SignInResultCred.Google -> {
//                                    signInViewModel.authenticateWithGoogle(
//                                        res.idToken,
//                                        onSuccess = { navigateToRegistration() },
//                                        onError = { msg ->
//                                            Toast
//                                                .makeText(context, msg, Toast.LENGTH_SHORT)
//                                                .show()
//                                        }
//                                    )
//                                }
//                                SignInResultCred.Cancelled -> {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "Back",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//
//                                }
//                                SignInResultCred.Failure -> {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "Failure",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//                                SignInResultCred.NoCredentials -> {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "No credentials",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//                                is SignInResultCred.Password -> {
//                                    Toast
//                                        .makeText(
//                                            context,
//                                            "Password",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                        .show()
//                                }
//                            }
//                        }
//                    }
//            ) {

//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        modifier = Modifier.size(25.dp),
//                        painter = painterResource(id = R.drawable.google_signin_icon),
//                        contentDescription = null,
//                        tint = Color.Unspecified
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = stringResource(id = R.string.sign_in_with_google),
//                        style = TextStyle(
//                            fontFamily = Poppins,
//                            fontWeight = FontWeight.SemiBold,
//                            fontSize = 14.sp
//                        ),
//                        textAlign = TextAlign.Center,
//                        color = textGoogleButtonColor()
//                    )
//                }
//            }

        }
    }
}
