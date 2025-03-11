package com.example.gymbuddy.pushnotification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymbuddy.buttonState.ButtonStateManager
import com.example.gymbuddy.chat.Message
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.FriendRequestRepositoryImpl
import com.example.gymbuddy.friends.FriendInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRequestRepository: FriendRequestRepositoryImpl,
    private val buttonStateManager: ButtonStateManager,
    private val fcmApi: FcmApi
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _friendsRequestStatus = MutableStateFlow(FriendRequestInformationDto())
    val friendsRequestStatus: StateFlow<FriendRequestInformationDto> =
        _friendsRequestStatus.asStateFlow()

    // Lista zaproszeń (surowe dane zaproszeń, np. senderId, receiverId, status)
    private val _friendsRequestList =
        MutableStateFlow<List<FriendRequestInformationDto>>(emptyList())
    val friendsRequestList: StateFlow<List<FriendRequestInformationDto>> =
        _friendsRequestList.asStateFlow()

    // Pełna lista zaproszeń z danymi użytkownika (np. imię, nazwisko, zdjęcie profilowe itd.)
    private val _friendsRequestFullList = MutableStateFlow<List<UserFoundInformation>>(emptyList())
    val friendsRequestFullList: StateFlow<List<UserFoundInformation>> =
        _friendsRequestFullList.asStateFlow()

    private val _friendsList = MutableStateFlow<List<UserFoundInformation>>(emptyList())
    val friendList: StateFlow<List<UserFoundInformation>> = _friendsList.asStateFlow()

    val buttonState: StateFlow<String> = buttonStateManager.buttonState

    private val _friendInformation = MutableStateFlow(FriendInformation())
    val friendInformation: StateFlow<FriendInformation> = _friendInformation.asStateFlow()

    init {
        Firebase.auth.currentUser?.uid?.let { uid ->
            getAllFriend(uid)
        } ?: run {
            Log.e("FriendRequestViewModel", "Brak zalogowanego użytkownika")
        }
    }

    fun transferDataFromFoundUserToFriendRequest(receiverID: UserFoundInformation) {
        _friendsRequestStatus.update { currentState ->
            currentState.copy(
                receiverFcmToken = receiverID.fcmToken,
                senderId = firebaseAuth.currentUser!!.uid,
                receiverId = receiverID.userId
            )
        }
    }

    suspend fun addFriendRequestToDatabase(onSuccess: () -> Unit, onFailure: () -> Unit) {
        friendRequestRepository.addFriendRequestToDataBase(
            friendRequest = _friendsRequestStatus.value,
            onSuccess = {
                onSuccess()
            },
            onFailure = {
                onFailure()
            }
        )
    }

    fun getAllFriend(currentUserId: String) {
        viewModelScope.launch {
            try {
                val result = friendRequestRepository.getAllFriend(currentUserId)
                if (result.isSuccess) {
                    _friendsList.value = result.getOrNull() ?: emptyList()
                    println("friends: ${_friendsList.value}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAllFriendRequests(ownerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendRequestRepository.getAllFriendRequests(ownerId)
                if (result.isSuccess) {
                    val friendRequestsList = result.getOrNull() ?: emptyList()
                    _friendsRequestList.value = friendRequestsList
                    onSuccess()
                } else {
                    Log.e(
                        "FriendRequestViewModel",
                        "Error fetching friend requests: ${result.exceptionOrNull()}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAllFullFriendsRequestInformation() {
        viewModelScope.launch {
            try {
                val result =
                    friendRequestRepository.getAllFullFriendsRequestInformation(_friendsRequestList.value)
                if (result.isSuccess) {
                    val fullFriendsList = result.getOrNull() ?: emptyList()
                    _friendsRequestFullList.value = fullFriendsList
                } else {
                    Log.e(
                        "FriendRequestViewModel",
                        "Error fetching friend requests: ${result.exceptionOrNull()}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFriend(currentUserId: String, friendId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendRequestRepository.deleteFriend(currentUserId, friendId)
                if (result.isSuccess) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addFriendsInformationToDatabase(
        currentUserId: String,
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result =
                    friendRequestRepository.addFriendsInformationToDatabase(currentUserId, friendId)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

     fun sendDeclineNotification(declineFriendRequestDto: DeclineFriendRequestDto) {
        viewModelScope.launch {
            try {
                fcmApi.sendDeclineNotification(
                    declineFriendRequestDto = declineFriendRequestDto
                )
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
    }

    // decline
    fun deleteFriendRequestAfterAcceptingOrDecliningFriendRequestAndRefresh(
        currentUserId: String,
        friendId: String,
        onFailure: () -> Unit,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val result =
                    friendRequestRepository.deleteFriendRequestAfterAcceptingOrDecliningRequest(
                        currentUserId,
                        friendId
                    )
                if (result.isSuccess) {
                    fetchAllFriendRequestsAndFullInformation(currentUserId)
                    onSuccess()
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchAllFriendRequestsAndFullInformation(ownerId: String) {
        viewModelScope.launch {
            getAllFriendRequests(ownerId, onSuccess = {
                getAllFullFriendsRequestInformation()
            })
        }
    }

    // todo dziala prawie idealnie, jest lekkie opoznienie w zmianie stanu przycisku, kiedys do tego wroce
    // todo dziala ale podczas odswiezania to na ktora chwile widzimy Send Request a dopiero potem Send Message

    // tutaj bedzie cala logika sprawdzania stanu przycisku
    fun determineButtonState(friendId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = friendRequestRepository.determineButtonState(friendId)
                println(result)
                result.onSuccess { newState ->
                    ButtonStateManager.updateState(newState)
                    onSuccess()
                }
                result.onFailure { e ->
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    fun sendFriendRequestToUser() {
        viewModelScope.launch {
            try {
                fcmApi.sendFriendRequest(_friendsRequestStatus.value)
                buttonStateManager.updateState("Request Sent")
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object FriendRequestModule {
    @Provides
    fun provideFriendRequestRepository(): FriendRequestRepositoryImpl {
        return FriendRequestRepositoryImpl()
    }

    @Provides
    fun provideButtonStateManager(): ButtonStateManager {
        return ButtonStateManager
    }
}

