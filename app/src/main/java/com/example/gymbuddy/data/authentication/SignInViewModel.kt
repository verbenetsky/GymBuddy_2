package com.example.gymbuddy.data.authentication

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

class SignInViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userData: MutableStateFlow<UserData?> = MutableStateFlow(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _state = MutableStateFlow(SingInState())
    val state = _state.asStateFlow()

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState.asStateFlow()

    private val _isEmailWasUsed = MutableStateFlow<Boolean>(false)
    val isEmailWasUsed: StateFlow<Boolean> = _isEmailWasUsed.asStateFlow()

    private val _password = MutableStateFlow<String>("")
    val password: StateFlow<String> = _password.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = auth.currentUser
        println(user)
        if (user == null)
            _authState.value = AuthState.Unauthenticated
        else {
            val isGoogleUser = user.providerData.any { it.providerId == "google.com" }
            _authState.value = if (isGoogleUser) {
                AuthState.GoogleAuthenticated
            } else {
                AuthState.Authenticated
            }
        }
    }


    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                onSuccess()
            } catch (e: Exception) {
                _loginFormState.update { currentState ->
                    currentState.copy(
                        isLoginSuccessful = false,
                        password = ""
                    )
                }
                _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun clearLoginForm() {
        _loginFormState.value = LoginFormState()
    }

    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                _isEmailWasUsed.value = false
                onSuccess()
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthUserCollisionException -> {
                        _isEmailWasUsed.value = true
                        onError("Email jest już używany.")
                    }
                    else -> {
                        _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
                        onError(e.localizedMessage ?: "Unknown error")
                    }
                }
            }
        }
    }

    fun signOut() {
        val user = auth.currentUser
        if (user != null) {
            auth.signOut()
        }
        _authState.value = AuthState.Unauthenticated
    }

    fun updateEmail(newEmail: String) {
        _loginFormState.update { currentState ->
            currentState.copy(
                email = newEmail,
                isEmailValid = validateEmail(newEmail)
            )
        }
    }

    fun updatePassword(newPassword: String) {
        _loginFormState.update { currentState ->
            currentState.copy(
                password = newPassword,
                isPasswordValid = validatePassword(newPassword)
            )
        }
    }


    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    fun resetState() {
        _state.update { SingInState() }
    }

    private fun validateEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    private val EMAIL_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    private val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +
                "(?=.*[a-zA-Z])" +
                ".{8,}" +
                "$"
    )

    private fun validatePassword(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    fun analyzePasswordRequirementsOneDigit(password: String): Boolean {
        return password.any { it.isDigit() }
    }

    fun analyzePasswordRequirementsLength(password: String): Boolean {
        return 8 <= password.length
    }


    sealed class AuthState {
        object GoogleAuthenticated : AuthState()
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }

}