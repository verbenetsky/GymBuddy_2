package com.example.gymbuddy.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.model.Channel
import com.example.gymbuddy.data.model.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.ChannelRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepositoryImpl,
) : ViewModel() {

    val currentUser = Firebase.auth.currentUser!!.uid

    private val _channels = MutableStateFlow<Set<Channel>>(emptySet())
    val channels: StateFlow<Set<Channel>> = _channels.asStateFlow()

    // w ModalBottomSheet istnieje lista wszystkich znajomych, wiec tutaj
    // przechowujemy liste znajomych do stworzenia chatu z nimi
    private val _friendsWithoutChat = MutableStateFlow<Set<UserFoundInformation>>(emptySet())
    val friendsWithoutChat = _friendsWithoutChat.asStateFlow()

    init {
        observeChannels()
    }

    fun getLastMessage(channelId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val result = channelRepository.getLastMessage(channelId)
            if (result.isSuccess) {
                result.getOrNull()?.let { onSuccess(it) }
            } else {
                println("cannot get last message")
            }
        }
    }

    fun addChannel(friendId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val result = channelRepository.addChannel(
                Channel(
                    id = id,
                    lastMessage = "",
                    firstFriendId = friendId,
                    secondFriendId = Firebase.auth.currentUser!!.uid,
                    createdAt = System.currentTimeMillis(),
                )
            )
            if (result.isSuccess) {
                println("channel created")
                onSuccess()
            } else {
                println("cannot add a new channel")
            }
        }
    }

    suspend fun addChannelSuspend(friendId: String): String {
        val id = UUID.randomUUID().toString()
        val result = channelRepository.addChannel(
            Channel(
                id = id,
                lastMessage = "",
                firstFriendId = friendId,
                secondFriendId = Firebase.auth.currentUser!!.uid,
                createdAt = System.currentTimeMillis(),
            )
        )

        if (result.isFailure) {
            throw result.exceptionOrNull() ?: IllegalStateException("Cannot create channel")
        }
        return id
    }

    suspend fun findChannel(
        friendId: String,
    ): String {
        val res = channelRepository.findChannel(friendId, currentUser)
        return res.getOrNull() ?: ""
    }

    fun deleteChannel(
        channelId: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = channelRepository.deleteChannel(channelId)
            if (res.isSuccess) {
                onSuccess()
            } else {
                onFailure(res.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    suspend fun deleteAllChannels() {
        val res = channelRepository.deleteAllChannels(currentUser)
        if (res.isSuccess) {
            println("all channels (conversations) have been deleted")
        } else {
            println("problems with deleting channels")
        }
    }

    private fun observeChannels() {
        viewModelScope.launch {
            channelRepository
                .observeChannelsForUser(Firebase.auth.currentUser!!.uid)
                .collect { newSet ->
                    _channels.value = newSet
                }
        }
    }

    fun refreshFriendsWithoutChat(allFriends: List<UserFoundInformation>) {
        val withoutChat = channelRepository.computeFriendsWithoutChannel(
            allFriends = allFriends,
            existingChannels = _channels.value,
            currentUserId = Firebase.auth.currentUser!!.uid
        )
        _friendsWithoutChat.value = withoutChat
    }
}