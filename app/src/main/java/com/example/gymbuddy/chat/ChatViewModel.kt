package com.example.gymbuddy.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymbuddy.pushnotification.FcmApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val fcmApi: FcmApi,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val message = _messages.asStateFlow()
    private val db = Firebase.firestore

    fun sendImageMessage(
        uri: Uri,
        channelID: String,
        senderUsername: String,
        receiverFcmToken: String
    ) {
        val imageRef =
            Firebase.storage.reference.child("images/chat/$channelID/${UUID.randomUUID()}")
        imageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    sendMessage(
                        channelID = channelID,
                        messageText = null,
                        senderUsername = senderUsername,
                        image = downloadUri.toString(),
                        receiverFcmToken = receiverFcmToken
                    )
                    sendMessageNotification(
                        Message(
                            message = "Photo",
                            receiverFcmToken = receiverFcmToken,
                            senderName = senderUsername
                        )
                    )
                }
            }
    }

    fun sendMessage(
        channelID: String,
        messageText: String?,
        image: String? = null,
        receiverFcmToken: String,
        isShareWorkoutMessage: Boolean = false,
        senderUsername: String,
    ) {
        val docRef = db.collection("messages").document()

        val message = if (isShareWorkoutMessage) {
            Message(
                id = docRef.id,
                shareWorkoutMessage = true,
                senderId = Firebase.auth.currentUser?.uid ?: "",
                message = convertMarkdownToHtml(messageText ?: ""),
                channelId = channelID,
                receiverFcmToken = receiverFcmToken,
                createdAt = System.currentTimeMillis(),
                senderName = senderUsername,
                imageUrl = image,
                senderImage = null
            )
        } else {
            Message(
                id = docRef.id,
                senderId = Firebase.auth.currentUser?.uid ?: "",
                message = messageText ?: "",
                channelId = channelID,
                receiverFcmToken = receiverFcmToken,
                createdAt = System.currentTimeMillis(),
                senderName = senderUsername,
                imageUrl = image,
                senderImage = null
            )
        }


        sendMessageNotification(message)
        docRef.set(message)
            .addOnSuccessListener {
                updateLastMessage(channelID, messageText ?: "")
                println("Message sent with ID: ${docRef.id}")
            }
            .addOnFailureListener {
                println("Error sending message: $it")
            }
    }

    // usuwamy wszystkie wiadomosci i wyslane zdjecia dla konkretnego kanalu
    fun deleteAllMessagesForParticularChat(channelID: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("messages")
                    .whereEqualTo("channelId", channelID)
                    .get()
                    .await()
                val batch = db.batch()
                for (document in querySnapshot) {
                    batch.delete(document.reference)
                }
                batch.commit().await()
                println("All documents have been deleted.")
                deleteAllImagesForParticularChat(channelID)
            } catch (e: Exception) {
                println("Error during deletion: $e")
            }
        }
    }

    private fun sendMessageNotification(message: Message) {
        viewModelScope.launch {
            try {
                fcmApi.sendChatNotification(
                    message = message
                )
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun deleteAllImagesForParticularChat(channelID: String) {
        val directoryRef = Firebase.storage.reference.child("images/chat/$channelID")

        directoryRef.listAll() // listAll() zwraca wszystkie pliki znajdujece sie w tym folderze
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { fileRef ->
                    fileRef.delete()
                        .addOnSuccessListener {
                            println("Deleted: $it")
                        }
                        .addOnFailureListener { e ->
                            println("Error deleting file: $e")
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("Error listing files: $exception")
            }
    }

    private fun updateLastMessage(channelId: String, lastMessage: String) {
        db.collection("channels")
            .document(channelId)
            .update("lastMessage", lastMessage)
    }

    fun findChannelIdUsingUsersId(
        userID: String,
        onSuccess: (channelId: String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("channels")
            .whereEqualTo("firstFriendId", userID)
            .whereEqualTo("secondFriendId", Firebase.auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val channelId = querySnapshot.documents[0].id
                    onSuccess(channelId)
                } else {
                    db.collection("channels")
                        .whereEqualTo("secondFriendId", userID)
                        .whereEqualTo("firstFriendId", Firebase.auth.currentUser?.uid ?: "")
                        .get()
                        .addOnSuccessListener { secondSnapshot ->
                            if (secondSnapshot.documents.isNotEmpty()) {
                                val channelId = secondSnapshot.documents[0].id
                                onSuccess(channelId)
                            } else {
                                onSuccess(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    //pobieramy messages
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

    // todo delete this in the future, two instances in the code
    fun convertMarkdownToHtml(markdownText: String): String {
        // Używamy CommonMark – można też rozszerzyć lub zmienić flavour
        val flavour = CommonMarkFlavourDescriptor()
        // Parsujemy strukturę dokumentu Markdown
        val parser = MarkdownParser(flavour)
        val parsedTree = parser.buildMarkdownTreeFromString(markdownText)
        // Generujemy HTML na podstawie sparsowanego drzewa
        val htmlGenerator = HtmlGenerator(markdownText, parsedTree, flavour)
        return htmlGenerator.generateHtml()
    }
}