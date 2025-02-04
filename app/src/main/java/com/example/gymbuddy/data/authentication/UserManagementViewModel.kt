package com.example.gymbuddy.data.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserManagementViewModel(private val userRepository: UserRepositoryImpl = UserRepositoryImpl()) :
    ViewModel() {

    private val _userInformationState = MutableStateFlow(UserInformation())
    val userInformationState: StateFlow<UserInformation> = _userInformationState

    fun updateFirstName(firstName: String) {
        _userInformationState.update { currentState -> currentState.copy(firstName = firstName) }
    }

    fun updateLastName(lastName: String) {
        _userInformationState.update { currentState -> currentState.copy(lastName = lastName) }
    }

    fun updateUsername(username: String) {
        _userInformationState.update { currentState -> currentState.copy(username = username) }
    }

    fun updateDateOfBirth(dateOfBirth: Long) {
        _userInformationState.update { currentState -> currentState.copy(dateOfBirth = dateOfBirth) }
    }

    fun updateGoal(goal: String) {
        _userInformationState.update { currentState -> currentState.copy(goal = goal) }
    }

    fun removeHobby(hobby: String) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = currentState.hobbies - hobby) }
    }

    fun addHobby(hobby: String) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = currentState.hobbies + hobby) }
    }

    fun transportUserInformation(userData: UserData) {

        _userInformationState.update { currentState ->
            currentState.copy(
                userId = userData.userId,
                email = userData.email,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                username = currentState.username,
                profilePictureUrl = currentState.profilePictureUrl,
                dateOfBirth = currentState.dateOfBirth,
                hobbies = currentState.hobbies,
                goal = currentState.goal
            )
        }
    }

    fun addUser() {
        viewModelScope.launch {
            val userInformation = _userInformationState.value
            val result = userRepository.addUser(userInformation)
        }
    }

    fun deleteUser(){
        viewModelScope.launch {
            val userId = _userInformationState.value.userId
            val result = userRepository.deleteUser(userId)
        }
    }

    fun getUserFromFireStoreToViewModel() {
        viewModelScope.launch {

        }
    }

}


