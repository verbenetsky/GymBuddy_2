package com.example.gymbuddy.pushnotification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.FriendRequestRepositoryImpl
import com.example.gymbuddy.friends.FriendRequestInformationDto
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRequestRepository: FriendRequestRepositoryImpl,
    private val fcmApi: FcmApi
) : ViewModel() {

    // Pełna lista zaproszeń z danymi użytkownika (np. imię, nazwisko, zdjęcie profilowe itd.)
    private val _friendsRequestList = MutableStateFlow<List<UserFoundInformation>>(emptyList())
    val friendsRequestList: StateFlow<List<UserFoundInformation>> =
        _friendsRequestList.asStateFlow()

    private val _friendsList = MutableStateFlow<List<UserFoundInformation>>(emptyList())
    val friendList: StateFlow<List<UserFoundInformation>> = _friendsList.asStateFlow()

    private val _buttonState = MutableStateFlow<FriendRequestRepositoryImpl.FriendButtonState?>(null)
    val buttonState: StateFlow<FriendRequestRepositoryImpl.FriendButtonState?> = _buttonState.asStateFlow()

    fun sendFriendRequest(
        friendRequestDto: FriendRequestInformationDto,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = friendRequestRepository.sendFriendRequest(friendRequestDto)
            if (result.isSuccess) {
                onSuccess()
                println("Friend request successfully sent")
            } else {
                onFailure(result.exceptionOrNull()?.localizedMessage ?: "")
            }
        }
    }

    fun declineFriendRequest(
        currentUserId: String,
        friendId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val result = friendRequestRepository.declineFriendRequest(currentUserId, friendId)
            if (result.isSuccess) {
                println("Friend request declined")
                onSuccess()
            } else {
                println("Error while declining friend request")
                println(result.exceptionOrNull()?.localizedMessage ?: "X")
                onFailure()
            }
        }
    }

    fun acceptFriendRequest(currentUserId: String, friendId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = friendRequestRepository.acceptFriendRequest(currentUserId, friendId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                println("Error while accepting friend request")
            }
        }
    }

    private var friendsJob: Job? = null
    fun startListeningFriends(currentUserId: String) {
        friendsJob = friendRequestRepository.observeFriends(currentUserId)
            .onEach { list -> _friendsList.value = list }
            .launchIn(viewModelScope)
    }

    fun stopListeningFriends() {
        friendsJob?.cancel()
    }

    private var listenerJob: Job? = null
    fun startListeningForRequests(currentUserId: String) {
        listenerJob = friendRequestRepository.observeIncomingFriendRequests(currentUserId)
            .onEach { list -> _friendsRequestList.value = list }
            .launchIn(viewModelScope)
    }

    fun stopListening() {
        listenerJob?.cancel()
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

    // tutaj bedzie cala logika sprawdzania stanu przycisku
    private var observeJob: Job? = null
    fun startObservingButton(currentUserId: String, searchedUserId: String) {
        observeJob?.cancel()
        observeJob = friendRequestRepository.observeButtonState(currentUserId, searchedUserId)
            .onEach { _buttonState.value = it }
            .launchIn(viewModelScope)
    }

    fun stopObservingButton() {
        observeJob?.cancel()
    }

//--------------------------------Sending Notification----------------------------------------------

    fun sendDeclineNotification(declineFriendRequestDto: AcceptOrDeclineOrRemoveFriendDto) {
        println("wysylanie DeclineNotification do usera")
        viewModelScope.launch {
            try {
                fcmApi.sendDeclineNotification(
                    declineFriendRequestDto = declineFriendRequestDto
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                println(e)
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                println(e)
            }
        }
    }

    fun sendRemoveNotification(removeDto: AcceptOrDeclineOrRemoveFriendDto) {
        viewModelScope.launch {
            println("wysylanie RemoveNotification do usera")
            try {
                fcmApi.sendRemoveNotification(
                    removeDto = removeDto
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                println(e)
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                println(e)

            }
        }
    }

    fun sendAcceptNotification(acceptFriendRequestDto: AcceptOrDeclineOrRemoveFriendDto) {
        viewModelScope.launch {
            try {
                println("wysylanie AcceptNotification do usera")
                fcmApi.sendAcceptNotification(
                    acceptFriendRequestDto = acceptFriendRequestDto
                )
            } catch (e: HttpException) {
                e.printStackTrace()
                println(e)
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                println(e)
            }
        }
    }

    fun sendFriendRequestToUser(friendRequestDto: FriendRequestDto) {
        viewModelScope.launch {
            try {
                println("wysylanie FriendRequest do usera")
                fcmApi.sendFriendRequest(friendRequestDto)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

//--------------------------------------------------------------------------------------------------

@Module
@InstallIn(ViewModelComponent::class)
object FriendRequestModule {
    @Provides
    fun provideFriendRequestRepository(): FriendRequestRepositoryImpl {
        return FriendRequestRepositoryImpl()
    }
}
//--------------------------------------------------------------------------------------------------


