package com.example.gymbuddy.data.authentication


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymbuddy.repository.AuthRepository

class SignInViewModelFactory(
    private val authRepo: AuthRepository,
    private val accountManager: CredentialManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            return SignInViewModel(authRepo, accountManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
