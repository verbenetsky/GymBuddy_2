package com.example.gymbuddy.data.authentication

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserSearchViewModel(
    private val userRepository: UserRepositoryImpl = UserRepositoryImpl(),
) : ViewModel() {

    private val _userFoundInformation = MutableStateFlow(UserFoundInformation())
    val userFoundInformation: StateFlow<UserFoundInformation> = _userFoundInformation.asStateFlow()

    private val _userSearchState = MutableStateFlow<UserSearchState>(UserSearchState.NothingFound)
    val userSearchState: StateFlow<UserSearchState> = _userSearchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private fun updateFirstNameOfFoundUser(firstName: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                firstName = firstName
            )
        }
    }

    private fun updateLastNameOfFoundUser(lastName: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                lastName = lastName
            )
        }
    }

    private fun updateUsernameOfFoundUser(username: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                username = username
            )
        }
    }

    private fun updateProfilePictureUrlOfFoundUser(profilePictureUrl: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                profilePictureUrl = profilePictureUrl
            )
        }
    }

    fun resetUserFoundInformation() {
        _userFoundInformation.value = UserFoundInformation()
    }

    fun updateUserSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchState(state: UserSearchState) {
        _userSearchState.value = state
    }

    fun searchUser(
        username: String,
        onSuccessSearch: () -> Unit,
        onFailureSearch: () -> Unit
    ) {
        viewModelScope.launch {
            updateSearchState(UserSearchState.Loading)
            val result = userRepository.searchUser(username)
            println(result)

            result.onSuccess { userList ->
                if (userList.isEmpty()) {
                    onFailureSearch()
                    println("Failure: search returned empty list")
                } else {
                    onSuccessSearch()
                    updateFirstNameOfFoundUser(userList[0].firstName)
                    updateLastNameOfFoundUser(userList[0].lastName)
                    updateUsernameOfFoundUser(userList[0].username)
                    updateProfilePictureUrlOfFoundUser(userList[0].profilePictureUrl)
                    println("Success: found ${userList.size} user(s)")
                }
            }
            result.onFailure { error ->
                onFailureSearch()
                println("Error searching user: ${error.message}")
            }
        }
    }

    sealed class UserSearchState {
        data object Loading : UserSearchState()
        data object NothingFound : UserSearchState()
        data object FoundUser : UserSearchState()
    }
}