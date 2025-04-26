package com.example.gymbuddy.data.authentication

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repositoryImpl.CloudStorageRepositoryImpl
import com.example.gymbuddy.data.repositoryImpl.UserManagementRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class UserManagementViewModel(
    private val userRepository: UserManagementRepositoryImpl = UserManagementRepositoryImpl(),
    private val cloudStorageRepository: CloudStorageRepositoryImpl = CloudStorageRepositoryImpl(),
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _userInformationState = MutableStateFlow(UserInformation())
    val userInformationState: StateFlow<UserInformation> = _userInformationState.asStateFlow()

    private val _imageState = MutableStateFlow<ImageState>(ImageState.LoadedImage)
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()


    init {
        fetchUserData()
    }

    // from Firestore to viewModel
    fun fetchUserData(logInOnly: Boolean = true) {
        if (auth.currentUser?.uid != null) {
            println("pobieranie danych z bazy")
            viewModelScope.launch {
                val userId = auth.currentUser?.uid
                if (logInOnly) {
                    if (userId == null) {
                        println("User is not authenticated")
                        return@launch
                    }
                    val result = userRepository.getUserFromFireStoreToViewModel(userId)
                    if (result.isSuccess) {
                        updateUserInformation(result.getOrThrow())
                    } else {
                        println("Error getting user data: ${result.exceptionOrNull()?.localizedMessage}")
                    }
                }

                val tokenResult = userRepository.getFcmToken()
                if (tokenResult.isSuccess) {
                    val token = tokenResult.getOrThrow()
                    updateUserFcmToken(token)
                    val addTokenResult = userRepository.addFcmTokenToDataBase(userId!!, token)
                    if (addTokenResult.isSuccess) {
                        println("Token added successfully")
                    } else {
                        println("Error adding token: ${addTokenResult.exceptionOrNull()?.localizedMessage}")
                    }
                } else {
                    println("Fetching FCM registration token failed: ${tokenResult.exceptionOrNull()?.localizedMessage}")
                }
            }
        } else {
            println("byla proba pobrac dane z firestore ale sie nie udalo")
        }
    }

    //------------------------------------Update Fields---------------------------------------------

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

    fun addHobbies(hobbies: List<String>) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = hobbies) }
    }

    //----------------------------------------------------------------------------------------------

    fun transportUserInformation(userData: UserData) {
        _userInformationState.update { currentState ->
            currentState.copy(
                userId = userData.userId,
                email = userData.email
            )
        }
    }

    private fun updateUserInformation(userInformation: UserInformation) {
        _userInformationState.value = userInformation
    }

    fun clearForm() {
        _userInformationState.update { UserInformation() }
    }


    fun addUser(
        userInformation: UserInformation,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        println(userInformation)
        viewModelScope.launch {
            val result = userRepository.addUser(userInformation)
            if (result.isSuccess)
                onSuccess()
            else
                onError(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
        }
    }

    fun deleteUserDataFromFirestore(
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        userId: String,
        username: String
    ) {
        viewModelScope.launch {
            val result = userRepository.deleteUser(userId)
            println(result)
            if (result.isSuccess) {
                deleteUsernameFromDataBase(username)
                clearForm()
                onSuccess()
                println("Deleted user from db successfully")
            } else {
                onFailure()
            }
        }
    }

    fun updateUser(
        newUserData: UserInformation,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onFailureAddUsername: () -> Unit,
        oldUsername: String,
        newUsername: String
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                println("user not authenticated")
                return@launch
            }

            val currentData = _userInformationState.value
            var finalUsername = currentData.username
            // tutaj sprawdzamy czy nie ma nowego username w bazie i jesli nie to dodajemy do kolekcji
            // zeby zapewnic unikatowosc
            if (oldUsername != newUsername) {
                val resultAddUsername = userRepository.addUsernameToDataBase(newUsername)
                if (resultAddUsername.isSuccess) {
                    finalUsername = newUsername
                    deleteUsernameFromDataBase(oldUsername)
                } else {
                    onFailureAddUsername()
                    finalUsername = currentData.username
                }
            }

            val updatedData = currentData.copy(
                firstName = newUserData.firstName,
                lastName = newUserData.lastName,
                username = finalUsername,
                dateOfBirth = newUserData.dateOfBirth,
                hobbies = newUserData.hobbies,
                goal = newUserData.goal
            )

            val result = userRepository.updateUser(updatedData, userId)
            if (result.isSuccess) {
                println("Data updated successfully")
                onSuccess()
            } else {
                println("Error updating data: ${result.exceptionOrNull()?.localizedMessage}")
                onFailure()
            }
        }
    }

    // registration new user screen
    fun addUsernameToDataBase(
        username: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit,
    ) {

        viewModelScope.launch {
            val result = userRepository.addUsernameToDataBase(username)
            if (result.isSuccess) {
                println("Username added successfully")
                onSuccess()
            } else {
                println("Error adding username to database: ${result.exceptionOrNull()?.message}")
                onFailure()
            }
        }
    }


    private fun deleteUsernameFromDataBase(username: String) {
        viewModelScope.launch {
            userRepository.deleteUsernameFromDataBase(username)
        }
    }

    fun updateProfilePictureToDefault() {
        _userInformationState.update { currentState ->
            currentState.copy(profilePictureUrl = "")
        }
    }

    private fun addProfilePictureUrlToViewModel(url: Uri) {
        _userInformationState.update { currentState ->
            currentState.copy(profilePictureUrl = url.toString())
        }
    }

    fun uploadProfilePicture(imageUri: Uri, userId: String) {
        if (imageUri != _userInformationState.value.profilePictureUrl.toUri()) {
            _imageState.value = ImageState.LoadingImage
            println("userId : $userId")
            viewModelScope.launch {
                val result = cloudStorageRepository.uploadImage(imageUri, userId)
                result.onSuccess { downloadUrl ->
                    addProfilePictureUrlToViewModel(downloadUrl.toUri())
                }.onFailure { error ->
                    println("Error uploading image: ${error.message}")
                }
                _imageState.value = ImageState.LoadedImage
            }
        }
    }

    fun deleteProfilePicture(imageUri: String) {
        if (userInformationState.value.profilePictureUrl == "") {
            println("profile picture Url is empty")
            return
        }
        viewModelScope.launch {
            val result = cloudStorageRepository.deleteImage(imageUri)
            if (result.isSuccess) {
                println("profile picture deleted")
            }
        }
    }

    private fun updateUserFcmToken(token: String) {
        _userInformationState.update { currentState ->
            currentState.copy(fcmToken = token)
        }
    }

    //----------------------------------------------------------------------------------------------

    private val lastAndFirstNamePattern: Pattern = Pattern.compile("^[a-zA-Z]*$")
    private val usernamePattern: Pattern = Pattern.compile("^[a-zA-Z0-9_.-]*$")

    sealed class ImageState {
        data object LoadedImage : ImageState()
        data object LoadingImage : ImageState()
    }
}


