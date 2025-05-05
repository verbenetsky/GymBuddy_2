package com.example.gymbuddy.deleteAll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.repository.AuthRepository
import com.example.gymbuddy.repository.ChannelRepository
import com.example.gymbuddy.repository.ChatBotRepository
import com.example.gymbuddy.repository.ChatRepository
import com.example.gymbuddy.repository.CloudStorageRepository
import com.example.gymbuddy.repository.FriendRequestRepository
import com.example.gymbuddy.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteAllViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val chatBotRepo: ChatBotRepository,
    private val channelRepo: ChannelRepository,
    private val frRepo: FriendRequestRepository,
    private val userRepo: UserManagementRepository,
    private val authRepo: AuthRepository,
    private val cloudRepo: CloudStorageRepository,
) : ViewModel() {

    private val _deletionState = MutableStateFlow<DeletionState>(DeletionState.Idle)
    val deletionState: StateFlow<DeletionState> = _deletionState

    fun deleteAccountAndData(
        userId: String,
        username: String,
        profilePicUrl: String
    ) {
        viewModelScope.launch {
            // 1) kanały
            val channels = chatRepo.getAllChannelsIdFromUser(userId).getOrElse {
                _deletionState.value = DeletionState.Error("błąd pobierania kanałów")
                return@launch
            }
            // 2) wiadomosci
            chatRepo.deleteAllMessagesRelatedToUser(channels).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania wiadomości")
                return@launch
            }
            // 3) czat z botem
            chatBotRepo.deleteChatBotConversation(userId).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania czatu z botem")
                return@launch
            }
            // 4) kanały (same metadane)
            channelRepo.deleteAllChannels(userId).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania kanałów")
                return@launch
            }
            // 6) zaproszenia
            frRepo.deleteFriendRequests(userId).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania zaproszeń")
                return@launch
            }
            // 7) zdjęcie i dane
            cloudRepo.deleteImage(profilePicUrl).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania zdjęcia")
                return@launch
            }
            userRepo.deleteUser(userId).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania danych")
                return@launch
            }
            userRepo.deleteUsernameFromDataBase(username).onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania username z database")
                return@launch
            }
            // 8) Auth
            authRepo.deleteUserAccount().onFailure {
                _deletionState.value = DeletionState.Error("błąd usuwania konta Auth")
                return@launch
            }
            _deletionState.value = DeletionState.Success
        }
    }
    sealed class DeletionState {
        object Idle: DeletionState()
        object Loading: DeletionState()
        data class Error(val message: String): DeletionState()
        object Success: DeletionState()
    }
}