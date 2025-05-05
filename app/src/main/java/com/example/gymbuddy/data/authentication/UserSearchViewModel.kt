package com.example.gymbuddy.data.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.UserManagementRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class UserSearchViewModel(
    private val userRepository: UserManagementRepositoryImpl = UserManagementRepositoryImpl(),
) : ViewModel() {

    private val _userFoundInformation = MutableStateFlow(UserFoundInformation())
    val userFoundInformation: StateFlow<UserFoundInformation> = _userFoundInformation.asStateFlow()

    private val _userSearchState = MutableStateFlow<UserSearchState>(UserSearchState.NothingFound)
    val userSearchState: StateFlow<UserSearchState> = _userSearchState.asStateFlow()

    private val _searchFieldValue = MutableStateFlow("")
    val searchFieldValue = _searchFieldValue.asStateFlow()

    fun updateSearchField(newValue: String) {
        _searchFieldValue.value = newValue
    }

    // Chat Screen
    fun getUserBasedOnUserId(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = userRepository.getUser(userId)
                if (result.isSuccess) {
                    val doc = result.getOrNull() ?: UserFoundInformation()
                    updateUserFoundInformation(doc)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // message screen
    suspend fun getUserBasedOnUserId(userId: String): UserFoundInformation? {
        return try {
            val result = userRepository.getUser(userId)
            if (result.isSuccess) {
                _userFoundInformation.value = result.getOrNull()!!
                result.getOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateUserFoundInformation(userFoundInformation: UserFoundInformation) {
        _userFoundInformation.update { userFoundInformation }
    }

    fun searchUser(
        username: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        onNoOneFound: () -> Unit = {}
    ) {
        viewModelScope.launch {
            updateSearchState(UserSearchState.Loading)
            val result = userRepository.searchUser(username)

            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    updateUserFoundInformation(user) // jesli znajdziemy kogos to updejtujemy UserFoundInformation
                    onSuccess()
                    println("userFound: ${result.getOrNull()}")
                } ?: run {
                    onNoOneFound()
                    updateSearchState(UserSearchState.NothingFound)
                }
            } else {
                onFailure()
                println("Error: ${result.exceptionOrNull()?.localizedMessage ?: ""}")
                updateSearchState(UserSearchState.NothingFound)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    fun updateSearchState(state: UserSearchState) {
        _userSearchState.value = state
    }

    sealed class UserSearchState {
        data object Loading : UserSearchState()
        data object NothingFound : UserSearchState()
        data object FoundUser : UserSearchState()
    }
}