package com.example.gymbuddy.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.repositoryImpl.ChannelRepositoryImpl
import com.example.gymbuddy.data.repositoryImpl.FriendRequestRepositoryImpl
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

    fun addChannel(friendId: String) {
        viewModelScope.launch {
            val result = channelRepository.addChannel(
                Channel(
                    id = UUID.randomUUID().toString(),
                    lastMessage = "",
                    firstFriendId = friendId,
                    secondFriendId = Firebase.auth.currentUser!!.uid,
                    createdAt = System.currentTimeMillis(),
                )
            )
            if (result.isSuccess) {
                Result.success(true)
            } else {
                println("cannot add a new channel")
            }
        }
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

//    // pobieramy liste znajomych z ktorymi mozna zaczac chat
//    fun getListOfFriendChat(
//        friendList: List<UserFoundInformation>,
//        channels: Set<Channel>
//    ) {
//        val friendsWithoutChat = mutableSetOf<UserFoundInformation>()
//        val setOfFriendsIdInChannels = mutableSetOf<String>()
//        //dostaniemy id wszystkich friends w czatach
//        for (channel in channels) {
//            if (channel.secondFriendId == currentUser)
//                setOfFriendsIdInChannels.add(channel.firstFriendId)
//            else
//                setOfFriendsIdInChannels.add(channel.secondFriendId)
//        }
//
//        for (friend in friendList) {
//            // jesli true to czat juz poczety
//            if (friend.userId in setOfFriendsIdInChannels) {
//                continue
//            } else {
//                friendsWithoutChat.add(friend)
//            }
//        }
//        _friendsWithoutChat.value = friendsWithoutChat
//    }

//    init {
//        getChannelsForParticularUser()
//    }

//    fun deleteChannel(channelId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
//        db.collection("channels")
//            .document(channelId)
//            .delete()
//            .addOnSuccessListener {
//                onSuccess()
//            }
//            .addOnFailureListener { exception ->
//                onFailure(exception)
//            }
//    }
//
//    fun getLastMessage(channelId: String, onSuccess: (String) -> Unit) {
//        val docRef = db.collection("channels")
//            .document(channelId)
//
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    val value = document.getString("lastMessage")
//                    if (value != null) {
//                        onSuccess(value)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                println("Error getting document: $exception")
//            }
//    }
//
//    // friends z ktorymi mozna stworzyc czat (z ktorymi nie byl jeszce stworzony)
//    fun getListOfFriendChat(
//        friendList: List<UserFoundInformation>,
//        channels: Set<Channel>
//    ) {
//        val setOfFriendsChat = mutableSetOf<UserFoundInformation>()
//        val setOfFriendsIdInChannels = mutableSetOf<String>()
//        //dostaniemy id wszystkich friends w czatach
//        for (channel in channels) {
//            if (channel.secondFriendId == currentUser)
//                setOfFriendsIdInChannels.add(channel.firstFriendId)
//            else
//                setOfFriendsIdInChannels.add(channel.secondFriendId)
//        }
//
//        for (friend in friendList) {
//            // jesli true to czat juz poczety
//            if (friend.userId in setOfFriendsIdInChannels) {
//                continue
//            } else {
//                setOfFriendsChat.add(friend)
//            }
//        }
//        _setOfFriendsChat.value = setOfFriendsChat
//    }
//
//    // pobieramy channels
//    private fun getChannelsForParticularUser() {
//        val channels = mutableSetOf<Channel>()
//
//        db.collection("channels")
//            .whereEqualTo("firstFriendId", currentUser)
//            .addSnapshotListener { querySnapshot, error ->
//                if (error != null) {
//                    println("Error fetching channels: $error")
//                    return@addSnapshotListener
//                }
//                if (querySnapshot != null) {
//                    for (document in querySnapshot.documents) {
//                        val id = document.id
//                        val firstFriendId = document.getString("firstFriendId") ?: ""
//                        val secondFriendId = document.getString("secondFriendId") ?: ""
//                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
//                        channels.add(Channel(id, firstFriendId, secondFriendId, createdAt))
//                    }
//                    _channels.value = channels
//                }
//            }
//
//        db.collection("channels")
//            .whereEqualTo("secondFriendId", currentUser)
//            .addSnapshotListener { querySnapshot, error ->
//                if (error != null) {
//                    println("Error fetching channels: $error")
//                    return@addSnapshotListener
//                }
//                if (querySnapshot != null) {
//                    for (document in querySnapshot.documents) {
//                        val id = document.id
//                        val firstFriendId = document.getString("firstFriendId") ?: ""
//                        val secondFriendId = document.getString("secondFriendId") ?: ""
//                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
//                        channels.add(Channel(id, firstFriendId, secondFriendId, createdAt))
//                    }
//                    _channels.value = channels
//                }
//            }
//    }
//
//    fun addChannel(friendId: String) {
//        val newChannel = Channel(
//            firstFriendId = currentUser,
//            secondFriendId = friendId,
//            createdAt = System.currentTimeMillis()
//        )
//        db.collection("channels")
//            .add(newChannel)
//            .addOnSuccessListener { documentReference ->
//                db.collection("channels")
//                    .document(documentReference.id)
//                    .update("id", documentReference.id)
//                    .addOnSuccessListener {
//                        getChannelsForParticularUser()
//                    }
//            }
//            .addOnFailureListener { e ->
//                println("Error adding channel $e")
//            }
//    }
}