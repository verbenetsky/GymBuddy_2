package com.example.gymbuddy.data.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.buttonState.ButtonStateManager
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.UserRepositoryImpl
import kotlinx.coroutines.delay
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

    fun clearUserFoundInformation() {
        _userFoundInformation.value = UserFoundInformation()
    }

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

    private fun updateUserIDOfFoundUser(userID: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                userId = userID
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

    private fun updateEmailOfFoundUser(email: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                email = email
            )
        }
    }

    private fun updateDateOfBirthOfFoundUser(dateOfBirth: Long) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                dateOfBirth = dateOfBirth
            )
        }
    }

    private fun updateHobbiesOfFoundUser(hobbies: List<String>) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                hobbies = hobbies
            )
        }
    }

    private fun updateGoalOfFoundUser(goal: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                goal = goal
            )
        }
    }

    private fun updateFcmTokenOfFoundUser(token: String) {
        _userFoundInformation.update { currentState ->
            currentState.copy(
                fcmToken = token
            )
        }
    }

    private fun updateUserFoundInfo(userFoundInformation: UserFoundInformation) {
        _userFoundInformation.value = userFoundInformation
    }

    fun getUserBasedOnUserId(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = userRepository.getUser(userId)
                if (result.isSuccess) {
                    val doc = result.getOrNull() ?: UserFoundInformation()
                    updateUserFoundInfo(doc)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
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
                    updateUserIDOfFoundUser(userList[0].userId)
                    onSuccessSearch()
                    updateFirstNameOfFoundUser(userList[0].firstName)
                    updateLastNameOfFoundUser(userList[0].lastName)
                    updateUsernameOfFoundUser(userList[0].username)
                    updateProfilePictureUrlOfFoundUser(userList[0].profilePictureUrl)
                    updateEmailOfFoundUser(userList[0].email)
                    updateDateOfBirthOfFoundUser(userList[0].dateOfBirth)
                    updateHobbiesOfFoundUser(userList[0].hobbies)
                    updateGoalOfFoundUser(userList[0].goal)
                    updateFcmTokenOfFoundUser(userList[0].fcmToken)
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