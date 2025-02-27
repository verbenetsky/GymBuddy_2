package com.example.gymbuddy.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val message = _messages.asStateFlow()
    private val db = Firebase.firestore

    fun sendMessage(channelID: String, messageText: String, senderUsername: String) {
        val docRef = db.collection("messages").document()

        val message = Message(
            id = docRef.id,
            senderId = Firebase.auth.currentUser?.uid ?: "",
            message = messageText,
            channelId = channelID,
            createdAt = System.currentTimeMillis().toString(),
            senderName = senderUsername,
            imageUrl = null,
            senderImage = null
        )
        docRef.set(message)
            .addOnSuccessListener {
                println("Message sent with ID: ${docRef.id}")
            }
            .addOnFailureListener {
                println("Error sending message: $it")
            }
    }

    fun listenForMessages(channelID: String) {
        db.collection("messages")
            // pobieramy tylko ten dokument ktorego pole channel id jest rowne channelId
            .whereEqualTo("channelId", channelID)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener // tutaj jesli bedzie wyjatek to wychodzimy z lambdy a nie z calej fun getListenMessages
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.map { it.toObject(Message::class.java)!! }
                    _messages.value = messages
                }
            }
    }
}