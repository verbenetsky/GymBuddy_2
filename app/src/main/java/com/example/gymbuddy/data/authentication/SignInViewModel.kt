package com.example.gymbuddy.data.authentication

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

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()

    private val _signInValidation = MutableStateFlow(SignInValidation())
    val signInValidation: StateFlow<SignInValidation> = _signInValidation.asStateFlow()

    private val _state = MutableStateFlow(SingInState())
    val state = _state.asStateFlow()

    private val _isEmailWasUsed = MutableStateFlow(false)
    val isEmailWasUsed: StateFlow<Boolean> = _isEmailWasUsed.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = auth.currentUser
        println("user: $user")
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
        println(authState.value)
    }


    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                onSuccess()
            } catch (e: Exception) {
                _signInValidation.update { currentState ->
                    currentState.copy(
                        isLoginSuccessful = false,
                    )
                }
                _password.value = ""
                _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun clearLoginForm() {
        _userData.update { currentState ->
            currentState.copy(
                email = "",
            )
        }
    }

    fun clearUserData() {
        _userData.value = UserData()
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
                val result = auth.createUserWithEmailAndPassword(email, password)
                    .await()
                val userId = result.user?.uid ?: throw Exception("User ID is null")
                _userData.update { currentState -> currentState.copy(userId = userId) }
                _isEmailWasUsed.value = false
                onSuccess()
                resetPassword()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                resetPassword()
                when (e) {
                    is FirebaseAuthUserCollisionException -> {
                        _isEmailWasUsed.value = true
                        _authState.value = AuthState.Error("Email is already in use.")
                        onError("Email is already in use.")
                    }

                    else -> {
                        _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
                        onError(e.localizedMessage ?: "Unknown error")
                    }
                }
            }
        }
    }

    fun logOut() {
        val user = auth.currentUser
        if (user != null) {
            auth.signOut()
        }
        _authState.value = AuthState.Unauthenticated
    }


    fun updateEmail(newEmail: String) {
        _userData.update { it.copy(email = newEmail) }
        _signInValidation.update { currentState ->
            currentState.copy(
                isEmailValid = validateEmail(
                    newEmail
                )
            )
        }
    }

    fun resetPassword() {
        _password.value = ""
    }

    private fun validateEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return passwordPattern.matcher(password).matches()
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _signInValidation.update { currentState ->
            currentState.copy(
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

    fun setIsLoginSuccessful() {
        _signInValidation.update { currentState ->
            currentState.copy(
                isLoginSuccessful = true,
            )
        }
    }

    fun deleteUser() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("User deleted successfully")
            } else {
                println("Error deleting user: ${task.exception?.message}")
            }
        }
    }

    fun analyzePasswordRequirementsOneDigit(password: String): Boolean {
        return password.any { it.isDigit() }
    }

    fun analyzePasswordRequirementsLength(password: String): Boolean {
        return 8 <= password.length
    }

    fun updateAuthStateToUnauthenticated() {
        _authState.value = AuthState.Unauthenticated
    }

    fun updateAuthStateToAuthenticated() {
        _authState.value = AuthState.Authenticated
    }

    fun updateAuthStateToLoading() {
        _authState.value = AuthState.Loading
    }

    private val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    private val passwordPattern: Pattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +
                "(?=.*[a-zA-Z])" +
                ".{8,}" +
                "$"
    )

    sealed class AuthState {
        data object GoogleAuthenticated : AuthState()
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
        data object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }

}