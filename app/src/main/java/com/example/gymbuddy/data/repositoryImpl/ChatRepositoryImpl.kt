package com.example.gymbuddy.data.repositoryImpl

import android.net.Uri
import com.example.gymbuddy.chat.Message
import com.example.gymbuddy.repository.ChatRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

class ChatRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : ChatRepository {

    override suspend fun sendImageMessage(
        uri: Uri,
        channelId: String,
        senderUsername: String,
        receiverFcmToken: String
    ): Result<Message> {
        return try {
            // 1) upload do Storage
            val ref = storage.reference.child("images/chat/$channelId/${UUID.randomUUID()}")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // 2) przygotuj obiekt Message
            val docRef = db.collection("messages").document()
            val message = Message(
                id               = docRef.id,
                senderId         = Firebase.auth.currentUser!!.uid,
                senderName       = senderUsername,
                receiverFcmToken = receiverFcmToken,
                message          = null,            // brak tekstu
                imageUrl         = downloadUrl,     // URL obrazka
                shareWorkoutMessage = false,
                createdAt        = System.currentTimeMillis(),
                channelId        = channelId
            )

            // 3) zapisz w Firestore
            docRef.set(message).await()

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(channelId: String, message: Message): Result<Message> {
        return try {
            db.collection("messages")
                .add(message)
                .await()

            Result.success(message)
        } catch (e: Exception) {
            println("error")
            Result.failure(e)
        }
    }

    override suspend fun deleteAllMessages(channelID: String): Result<Boolean> { // for particular chat
        return try {
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
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllImages(channelID: String): Result<Boolean> { // for particular chat
        return try {
            val dirRef = storage.reference.child("images/chat/$channelID")
            val list = dirRef.listAll().await()
            list.items.forEach { it.delete().await() }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastMessage(
        channelId: String,
        lastMessage: String
    ): Result<Boolean> {
        return try {
            db.collection("channels")
                .document(channelId)
                .update("lastMessage", lastMessage)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findChannel( // using id both users
        currentUserId: String,
        otherUserId: String
    ): String? { // zwraca id kanalu
        return try {
            val firstQuery = db.collection("channels")
                .whereEqualTo("firstFriendId", currentUserId)
                .whereEqualTo("secondFriendId", otherUserId)
                .get()
                .await()

            if (firstQuery.documents.isNotEmpty()) {
                return firstQuery.documents[0].id
            }

            val secondQuery = db.collection("channels")
                .whereEqualTo("firstFriendId", otherUserId)
                .whereEqualTo("secondFriendId", currentUserId)
                .get()
                .await()

            if (secondQuery.documents.isNotEmpty()) {
                return secondQuery.documents[0].id
            }

            println("cannot find a channel")
            return null

        } catch (e: Exception) {
            println("something gone wrong")
            null
        }
    }


    override fun listenForMessages(channelID: String): Flow<List<Message>> =
        callbackFlow {
            val sub = db.collection("messages")
                .whereEqualTo("channelId", channelID)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) close(err)
                    else {
                        val msgs = snap?.toObjects(Message::class.java).orEmpty()
                        trySend(msgs)
                    }
                }
            awaitClose { sub.remove() }
        }
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = Firebase.storage
}