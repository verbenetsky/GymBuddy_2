package com.example.gymbuddy.data.authentication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymbuddy.R
import com.example.gymbuddy.ui.theme.Poppins
import com.example.gymbuddy.ui.theme.googleButtonColorDark
import com.example.gymbuddy.ui.theme.googleButtonColorLight
import com.example.gymbuddy.utils.CommonUtils


@Composable
fun SignInScreen(
    state: SingInState,
    viewModel: SignInViewModel,
    onContinueSignInScreenClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val loginFormState by viewModel.loginFormState.collectAsState()

    val context = LocalContext.current
    // LE czesc korutyn, LE jest wywolany raz przy pierwszym uzyciu composable lub kiedy key zmienia sie
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(72.dp))


        Box {
            Image(
                painter = painterResource(id = CommonUtils.logoTheme()),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))



        OutlinedTextField(
            value = loginFormState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Enter your email") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
            },
            isError = !loginFormState.isEmailValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
        )
        if (!loginFormState.isEmailValid) {
            Text(
                text = stringResource(R.string.invalid_email),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onContinueSignInScreenClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 48.dp),
            shape = RoundedCornerShape(4.dp),
            enabled = loginFormState.isEmailValid && loginFormState.email.isNotEmpty()
        ) {
            Text(
                text = stringResource(R.string.continue_text),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Text(
            stringResource(R.string.dont_have_an_account),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable { onSignUpClick() })
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .border(1.5.dp, borderColor(), shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { onSignInClick() }
                )
                .background(Color.Transparent)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(id = R.drawable.google_signin_icon),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.sign_in_with_google),
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = textGoogleButtonColor()
                )
            }
        }
    }


}

//@Preview(showBackground = true)
//@Composable
//private fun SignInScreenPreview() {
//    val state = SingInState()
//    SignInScreen(
//        state = state,
//        onSignUpClick = { },
//        onSignInClick = { },
//        onContinueSignInScreenClick = { },
//        modifier = TODO(),
//    )
//}

@Composable
fun googleButton(): Color {
    return if (isSystemInDarkTheme()) {
        googleButtonColorDark
    } else {
        googleButtonColorLight
    }
}

@Composable
fun borderColor(): Color {
    return if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
}

@Composable
fun textGoogleButtonColor(): Color {
    return if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
}