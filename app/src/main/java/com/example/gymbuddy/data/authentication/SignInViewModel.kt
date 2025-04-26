package com.example.gymbuddy.data.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repositoryImpl.AuthRepositoryImpl
import com.example.gymbuddy.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SignInViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
) : ViewModel() {

    private val auth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()

    private val _signInValidation = MutableStateFlow(SignInValidation())
    val signInValidation: StateFlow<SignInValidation> = _signInValidation.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    var hasTriedAutoLogin by mutableStateOf(false)
        private set

    fun markAutoLoginTried() {
        hasTriedAutoLogin = true
    }

    fun markAutoLoginUnTried() {
        hasTriedAutoLogin = false
    }

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = auth.currentUser
        println("user: $user")
        if (user == null)
            _authState.value = AuthState.Unauthenticated
        else {
            _authState.value = AuthState.Authenticated
        }
        println(authState.value)
    }

    fun logIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.logIn(email, password)
                onSuccess()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onError("Invalid email and/or password.")
                println("Invalid email and/or password.")
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                println(e)
                _authState.value = AuthState.Unauthenticated
                onError(e.localizedMessage ?: "Unknown error")
            } finally {
                resetPassword()
            }
        }
    }

    fun tryLogInCredentials(accountManager: CredentialManager) {

    }

    // metoda do logowania i rejestracji za pomoca google konta
    // jesli nie ma przypisanego konta do konta google to utworzy
    // a jesli jest to zaloguje sie na istniejacy
    fun authenticateWithGoogle(token: String, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val isNew = authRepository.signInWithCredentials(token)
                onSuccess(isNew)
            } catch (e: Exception) {
                println(e)
                onError(e.localizedMessage ?: "")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun logOut(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val result = authRepository.logOut()
                    if (result.isSuccess) {
                        _authState.value = AuthState.Unauthenticated
                        onSuccess()
                        clearUserData()
                    } else {
                        onError(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
                    }
                } else {
                    onError("No one is logged into account")
                    println("No one is logged into account")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
                println(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                authRepository.signUp(email, password)
                onSuccess()
                resetPassword()
                _authState.value = AuthState.AuthenticatedButNotRegister
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Unauthenticated
                onError("Email is already in use")
                println("Email is already in use")
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Unknown error"
                _authState.value = AuthState.Unauthenticated
                onError(errorMessage)
                println(errorMessage)
            } finally {
                resetPassword()
            }
        }
    }

//                val result = auth.createUserWithEmailAndPassword(email, password).await()
//                val userId = result.user?.uid ?: throw Exception("User ID is null")
//                _userData.update { currentState -> currentState.copy(userId = userId) }
    // todo maybe i will need it

    fun deleteUserAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = authRepository.deleteUserAccount()
                if (result.isSuccess) {
                    _authState.value = AuthState.Unauthenticated
                    onSuccess()
                    println("User deleted successfully")
                } else {
                    onError(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
                    println(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
                println(e)
            }
        }
    }

    //--------------------------------------Update Email/Password-----------------------------------

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

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _signInValidation.update { currentState ->
            currentState.copy(
                isPasswordValid = validatePassword(newPassword)
            )
        }
    }

    //--------------------------------------Reset/Clear---------------------------------------------

    fun sendResetPasswordEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.sendResetPasswordEmail(email)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "")
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

    fun setUserData(email: String, userId: String) {
        _userData.value = UserData(email = email, userId = userId)
    }

    fun clearUserData() {
        _userData.value = UserData()
    }

    fun resetPassword() {
        _password.value = ""
    }

    //---------------------------Validation of password and email-----------------------------------

    fun analyzePasswordRequirementsOneDigit(password: String): Boolean {
        return password.any { it.isDigit() }
    }

    fun analyzePasswordRequirementsLength(password: String): Boolean {
        return 8 <= password.length
    }

    fun updateAuthState(authState: AuthState) {
        _authState.value = authState
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

    private fun validateEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return passwordPattern.matcher(password).matches()
    }

    //----------------------------------------------------------------------------------------------


    fun setAuthState(newState: AuthState) {
        _authState.value = newState
    }

    sealed class AuthState {
        data object GoogleAuthenticated : AuthState()
        data object AuthenticatedButNotRegister : AuthState()
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
        data object Loading : AuthState()
    }
}