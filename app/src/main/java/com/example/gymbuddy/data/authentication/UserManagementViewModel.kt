package com.example.gymbuddy.data.authentication

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.CloudStorageRepositoryImpl
import com.example.gymbuddy.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class UserManagementViewModel(
    private val userRepository: UserRepositoryImpl = UserRepositoryImpl(),
    private val cloudStorageRepository: CloudStorageRepositoryImpl = CloudStorageRepositoryImpl()
) :
    ViewModel() {


    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _userInformationState = MutableStateFlow(UserInformation())
    val userInformationState: StateFlow<UserInformation> = _userInformationState

    fun updateFirstName(firstName: String) {
        if (firstName.matches(lastAndFirstNamePattern.toRegex())) {
            _userInformationState.update { currentState -> currentState.copy(firstName = firstName) }
        }
    }

    fun updateLastName(lastName: String) {
        if (lastName.matches(lastAndFirstNamePattern.toRegex())) {
            _userInformationState.update { currentState -> currentState.copy(lastName = lastName) }
        }
    }

    fun updateUsername(username: String) {
        if (username.matches(usernamePattern.toRegex())) {
            _userInformationState.update { currentState -> currentState.copy(username = username) }
        }
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

    fun updateHobbies(hobbies: List<String>) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = hobbies) }
    }

    init {
        getUserFromFireStoreToViewModel()
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

    private fun updateUserInformation(userInformation: UserInformation) {
        _userInformationState.update { currentState ->
            currentState.copy(
                userId = userInformation.userId,
                firstName = userInformation.firstName,
                lastName = userInformation.lastName,
                username = userInformation.username,
                hobbies = userInformation.hobbies,
                goal = userInformation.goal,
                email = userInformation.email,
                profilePictureUrl = userInformation.profilePictureUrl,
                dateOfBirth = userInformation.dateOfBirth,
            )
        }
    }

    fun clearForm() {
        _userInformationState.value = UserInformation()
    }

    fun addUser() {
        viewModelScope.launch {
            val userInformation = _userInformationState.value
            userRepository.addUser(userInformation)
        }
    }

    fun deleteUserDataFromFirestore() {
        viewModelScope.launch {
            val userId = _userInformationState.value.userId
            userRepository.deleteUser(userId)
        }
    }

    fun updateUser(newUserData: UserInformation) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                db.collection("users").document(userId)
                    .set(newUserData)
                    .addOnSuccessListener {
                        println("Data has been updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        println("Error updating data: $exception")
                    }
            }
        }
    }

    fun addProfilePictureUrlToViewModel(url: android.net.Uri) {
        _userInformationState.update { currentState ->
            currentState.copy(profilePictureUrl = url.toString())
        }
    }

    fun uploadProfilePicture(imageUri: android.net.Uri) {
        viewModelScope.launch {
            cloudStorageRepository.uploadImage(imageUri)
        }
    }

    fun getUserFromFireStoreToViewModel() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userInformation = document.toObject(UserInformation::class.java)
                        if (userInformation != null) {
                            updateUserInformation(userInformation)
                        }
                    }
                }.addOnFailureListener { exception ->
                    println("Error getting user data: $exception")
                }
            }
        }
    }

    private val lastAndFirstNamePattern: Pattern = Pattern.compile("^[a-zA-Z]*$")
    private val usernamePattern: Pattern = Pattern.compile("^[a-zA-Z0-9_.-]*$")

}


