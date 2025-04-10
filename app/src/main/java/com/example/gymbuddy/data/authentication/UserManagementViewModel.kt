package com.example.gymbuddy.data.authentication

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repositoryImpl.CloudStorageRepositoryImpl
import com.example.gymbuddy.data.repositoryImpl.UserManagementRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject


class UserManagementViewModel(
    private val userRepository: UserManagementRepositoryImpl = UserManagementRepositoryImpl(),
    private val cloudStorageRepository: CloudStorageRepositoryImpl = CloudStorageRepositoryImpl(),
) :
    ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _userInformationState = MutableStateFlow(UserInformation())
    val userInformationState: StateFlow<UserInformation> = _userInformationState.asStateFlow()

    private val _imageState = MutableStateFlow<ImageState>(ImageState.LoadedImage)
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()

    private val _usernameIsUsed = MutableStateFlow(false)
    val usernameIsUsed: StateFlow<Boolean> = _usernameIsUsed.asStateFlow()

    private val _bufferUserName: MutableStateFlow<String> = MutableStateFlow("")
    val bufferUserName: StateFlow<String> = _bufferUserName.asStateFlow()

    var oldUserName = ""

    init {
        getUserFromFirestoreToViewModel()
    }

    // todo fix
//    fun getUserFromFirestoreToViewModel() {
//        viewModelScope.launch {
//            val userId = auth.currentUser?.uid
//            if (userId != null) {
//                db.collection("users")
//                    .document(userId)
//                    .get()
//                    .addOnSuccessListener { document ->
//                        if (document.exists()) {
//                            val userInformation = document.toObject(UserInformation::class.java)
//                            if (userInformation != null) {
//                                updateUserInformation(userInformation)
//                            }
//                        }
//                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                            if (!task.isSuccessful) {
//                                println("Fetching FCM registration token failed: ${task.exception}")
//                                return@addOnCompleteListener
//                            }
//                            val token = task.result
//                            println("token: $token")
//                            updateUserFcmToken(token)
//
//                            viewModelScope.launch {
//                                userRepository.addFcmTokenToDataBase(
//                                    userId = userId,
//                                    token = token,
//                                    onSuccess = { println("token added successfully") },
//                                    onFailure = { exception -> println("Error adding token: $exception") }
//                                )
//                            }
//                        }
//                    }.addOnFailureListener { exception ->
//                        println("Error getting user data: $exception")
//                    }
//            }
//        }
//    }

    fun getUserFromFirestoreToViewModel(logInOnly: Boolean = true) {
        println("pobieranie danych z bazy")
        viewModelScope.launch {
            val auth: FirebaseAuth = Firebase.auth
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
    }

    //------------------------------------Update Fields---------------------------------------------
    fun updateUsernameIsUsed(isUsed: Boolean) {
        _usernameIsUsed.value = isUsed
    }

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

    fun updateBufferUsername(username: String) {
        if (username.matches(usernamePattern.toRegex())) {
            _bufferUserName.value = username
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

    fun updateHobbies(hobbies: List<String>) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = hobbies) }
    }

    fun updateToOldUsername() {
        _userInformationState.update { currentState ->
            currentState.copy(username = oldUserName)
        }
    }

    //----------------------------------------------------------------------------------------------

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
        _userInformationState.value = userInformation
    }


    fun clearForm() {
        _userInformationState.value = UserInformation()
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

    fun deleteUserDataFromFirestore() {
        viewModelScope.launch {
            val userId = _userInformationState.value.userId
            userRepository.deleteUser(userId)
        }
    }

    // todo fix this
//    fun updateUser(newUserData: UserInformation) {
//        viewModelScope.launch {
//            val userId = auth.currentUser?.uid
//            if (userId != null) {
//                db.collection("users").document(userId)
//                    .set(newUserData)
//                    .addOnSuccessListener {
//                        println("Data has been updated successfully")
//                    }
//                    .addOnFailureListener { exception ->
//                        println("Error updating data: $exception")
//                    }
//            }
//        }
//    }

    fun updateUser(newUserData: UserInformation, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                println("user not authenticated")
                return@launch
            }

            val result = userRepository.updateUser(newUserData, userId)
            if (result.isSuccess) {
                println("Data updated successfully")
                onSuccess()
            } else {
                println("Error updating data: ${result.exceptionOrNull()?.localizedMessage}")
                onFailure()
            }
        }
    }

    fun addUsernameToDataBase(
        username: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onEmptyUsername: () -> Unit,
    ) {

        if (_bufferUserName.value == "" && _userInformationState.value.username == "") { // todo
            onEmptyUsername()
            return
        }

        if (_bufferUserName.value != _userInformationState.value.username) {
            viewModelScope.launch {
                val result = userRepository.addUsernameToDataBase(username)
                if (result.isSuccess) {
                    println("Username added successfully")
                    updateUsernameIsUsed(false)
                    deleteUsernameFromDataBase(oldUserName)
                    onSuccess()
                } else {
                    println("Error adding username to database: ${result.exceptionOrNull()?.message}")
                    updateUsernameIsUsed(true)
                    onFailure()
                }
            }
        }
    }


    fun deleteUsernameFromDataBase(username: String) {
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

    fun uploadProfilePicture(imageUri: Uri) {
        if (imageUri != _userInformationState.value.profilePictureUrl.toUri()) {
            viewModelScope.launch {
                _imageState.value = ImageState.LoadingImage
                val result = cloudStorageRepository.uploadImage(imageUri)
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
        if (userInformationState.value.profilePictureUrl == "") return
        viewModelScope.launch {
            cloudStorageRepository.deleteImage(imageUri)
        }
    }

    fun updateUserFcmToken(token: String) {
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


