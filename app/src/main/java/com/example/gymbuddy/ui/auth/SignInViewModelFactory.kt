package com.example.gymbuddy.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymbuddy.data.repository.AuthRepository

class SignInViewModelFactory(
    private val authRepo: AuthRepository,
    private val accountManager: CredentialManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepo, accountManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
