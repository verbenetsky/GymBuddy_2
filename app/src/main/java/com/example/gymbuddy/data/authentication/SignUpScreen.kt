package com.example.gymbuddy.data.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gymbuddy.R
import com.example.gymbuddy.utils.CommonUtils

@Composable
fun SignUpScreen(
    navigateToProfile: () -> Unit,
    viewModel: SignInViewModel,
    onHaveAnAccountClick: () -> Unit,
    onCreateAnAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val loginFormState by viewModel.loginFormState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearLoginForm()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))


        Image(
            painter = painterResource(id = CommonUtils.logoTheme()),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = stringResource(R.string.create_an_account),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = loginFormState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email address") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
            },
        )

        if (viewModel.isEmailWasUsed.collectAsState().value) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.email_already_used),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = loginFormState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Enter your password") },
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

        if (!loginFormState.isPasswordValid) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.password_requirements),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(R.string.password_length),
                color = if (viewModel.analyzePasswordRequirementsLength(loginFormState.password)) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = stringResource(R.string.password_one_digit),
                color = if (viewModel.analyzePasswordRequirementsOneDigit(loginFormState.password)) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.signUp(
                    loginFormState.email, loginFormState.password,
                    onSuccess = { navigateToProfile() },
                    onError = { }
                )

                onCreateAnAccountClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 48.dp),
            shape = RoundedCornerShape(4.dp),
            enabled = (loginFormState.isEmailValid && loginFormState.isPasswordValid && loginFormState.password.length > 7)
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