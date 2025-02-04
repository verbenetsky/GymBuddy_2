package com.example.gymbuddy.data.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import com.example.gymbuddy.utils.CommonUtils
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SignInScreen2(
    viewModel: SignInViewModel,
    onDontHaveAnAccountClick: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onEditClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    val userData by viewModel.userData.collectAsState()
    val validation by viewModel.signInValidation.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val password by viewModel.password.collectAsState()


    LaunchedEffect(key1 = authState) {
        if (authState is SignInViewModel.AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(key1 = Unit) {
        keyboardController?.hide()
        viewModel.setIsLoginSuccessful()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Image(
                painter = painterResource(id = CommonUtils.logoTheme()),
                contentDescription = null,
                modifier = Modifier.size(195.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.enter_your_password),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userData.email,
            onValueChange = { /* Puste, bo email z poprzedniego kroku (readOnly) */ },
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
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Enter your password") },
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

        if (!validation.isLoginSuccessful) {
            Text(
                text = stringResource(R.string.wrong_email_or_password),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }



        Text(
            text = stringResource(R.string.forgot_password),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable { onForgetPasswordClick() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Główna akcja LOGOWANIA:
        Button(
            onClick = {
                // Wywołaj logowanie w ViewModelu
                viewModel.signIn(
                    email = userData.email,
                    password = password,
                    onSuccess = {
                        // Możesz tu zrobić jakąś dodatkową akcję
                        // ale ogólnie i tak mamy LaunchedEffect, który nasłuchuje AuthState
                    },
                    onError = { errorMsg ->
                        // Tutaj możesz np. pokazać Toast lub SnackBar
                        // żeby poinformować użytkownika o błędzie
                        println("Sign in error: $errorMsg")
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 48.dp),
            shape = RoundedCornerShape(4.dp),
            enabled = validation.isPasswordValid && password.isNotEmpty() && userData.email.isNotEmpty()
        ) {
            Text(
                text = stringResource(R.string.continue_text),
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

