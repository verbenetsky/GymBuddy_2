package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto
import com.example.gymbuddy.friends.SendingRequestStatus
import com.example.gymbuddy.repository.FriendRequestRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendRequestRepositoryImpl : FriendRequestRepository {

    private val db = Firebase.firestore

    override suspend fun sendFriendRequest(friendRequest: FriendRequestInformationDto): Result<Boolean> {
        return try {
            db.collection("friendships")
                .document()
                .set(friendRequest)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    enum class FriendButtonState { SendRequest, RequestSent, Decline, Remove }

    override fun observeButtonState(
        currentUserId: String,
        searchedUserId: String
    ): Flow<FriendButtonState> = callbackFlow {
        val registration = db.collection("friendships")
            // bierzemy wszystkie dokumenty, gdzie obaj użytkownicy są nadawcą/odbiorcą w dowolnej kolejności
            .whereIn("senderId", listOf(currentUserId, searchedUserId))
            .whereIn("receiverId", listOf(currentUserId, searchedUserId))
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val docs = snap?.documents.orEmpty()

                // Jeżeli jest dokument ze statusem ACCEPTED → Remove
                val hasAccepted = docs.any { it.getString("status") == SendingRequestStatus.ACCEPTED.name }
                // Jeżeli jest dokument PENDING wysłany przez currentUser → RequestSent
                val hasSent    = docs.any {
                    it.getString("status") == SendingRequestStatus.PENDING.name &&
                            it.getString("senderId") == currentUserId
                }
                // Jeżeli jest dokument PENDING wysłany przez drugiego usera → Decline
                val hasRecv    = docs.any {
                    it.getString("status") == SendingRequestStatus.PENDING.name &&
                            it.getString("senderId") == searchedUserId
                }

                val state = when {
                    hasAccepted         -> FriendButtonState.Remove
                    hasSent             -> FriendButtonState.RequestSent
                    hasRecv             -> FriendButtonState.Decline
                    else                -> FriendButtonState.SendRequest
                }

                trySend(state).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override fun observeFriends(currentUserId: String): Flow<List<UserFoundInformation>> = callbackFlow {

        fun launchFetchAndSend(ids: Set<String>) {
            // odpalenie w korutynie callbackFlow
            launch {
                val users = if (ids.isEmpty()) {
                    emptyList()
                } else {
                    ids.chunked(10)
                        .flatMap { chunk ->
                            db.collection("users")
                                .whereIn(FieldPath.documentId(), chunk)
                                .get()
                                .await()
                                .toObjects(UserFoundInformation::class.java)
                        }
                }
                trySend(users)
            }
        }
        // zbiór ID znajomych
        val friendIds = mutableSetOf<String>()

        // listener dla przyjaźni, gdzie currentUser jest senderem
        val regSent = db.collection("friendships")
            .whereEqualTo("status", SendingRequestStatus.ACCEPTED.name)
            .whereEqualTo("senderId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                // dodaj albo usuń sender->receiver ID
                snapshot?.run {
                    friendIds.clear()
                    // najpierw wyczyść i zbierz z obu zapytań na nowo
                    documents.forEach { doc ->
                        doc.getString("receiverId")?.let { friendIds.add(it) }
                    }
                    // teraz dokleimy jeszcze z drugiego listenera poniżej
                }
                // fetchujemy profile
                launchFetchAndSend(friendIds)
            }

        // listener dla przyjaźni, gdzie currentUser jest receiverem
        val regRecv = db.collection("friendships")
            .whereEqualTo("status", SendingRequestStatus.ACCEPTED.name)
            .whereEqualTo("receiverId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.run {
                    friendIds.clear()
                    documents.forEach { doc ->
                        doc.getString("senderId")?.let { friendIds.add(it) }
                    }
                }
                launchFetchAndSend(friendIds)
            }

        awaitClose {
            regSent.remove()
            regRecv.remove()
        }
    }

    override suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Boolean> {
        return try {
            val docs = db.collection("friendships")
                .whereEqualTo("status", SendingRequestStatus.ACCEPTED.name)
                .whereIn("senderId", listOf(currentUserId, friendId))
                .whereIn("receiverId", listOf(currentUserId, friendId))
                .get()
                .await()

            // usuń wszystkie znalezione dokumenty
            docs.documents.forEach { it.reference.delete().await() }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeIncomingFriendRequests(
        currentUserId: String
    ): Flow<List<UserFoundInformation>> = callbackFlow {
        // Dodajemy listenera do collection "friendships"
        val registration: ListenerRegistration = db.collection("friendships")
            .whereEqualTo("status", SendingRequestStatus.PENDING.name)
            .whereEqualTo("receiverId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)        // zamknij flow przy błędzie
                    return@addSnapshotListener
                }
                // zebrane senderId
                val senderIds = snapshot
                    ?.documents
                    ?.mapNotNull { it.getString("senderId") }
                    ?.toSet()
                    .orEmpty()

                // jeśli nie ma zaproszeń, wyślij pustą listę
                if (senderIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // batch‐fetch profilów użytkowników (po max 10 na raz)
                launch {
                    val chunks = senderIds.chunked(10)
                    val users = chunks.flatMap { chunk ->
                        db.collection("users")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()   // już legalnie wewnątrz korutyny
                            .toObjects(UserFoundInformation::class.java)
                    }
                    trySend(users)
                }
            }

        // Kiedy odbiorca Flow przestanie subskrybować, usuwamy listenera:
        awaitClose { registration.remove() }
    }

    override suspend fun declineFriendRequest(
        currentUserId: String,
        senderId: String
    ): Result<Boolean> {
        return try {
            val querySnapshot =
                db.collection("friendships")
                    .whereEqualTo("status", "PENDING")
                    .whereEqualTo("receiverId", currentUserId)
                    .whereEqualTo("senderId", senderId)
                    .get()
                    .await()

            if (querySnapshot.documents.isEmpty()) {
                return Result.failure(Exception("Brak zaproszenia o statusie PENDING"))
            }

            querySnapshot.documents.first().reference.update("status", "DECLINED").await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(
        currentUserId: String,
        senderId: String
    ): Result<Boolean> {
        return try {
            val querySnapshot = db.collection("friendships")
                .whereEqualTo("status", SendingRequestStatus.PENDING.name)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("senderId", senderId)
                .get()
                .await()

            if (querySnapshot.documents.isEmpty()) {
                return Result.failure(Exception("Brak zaproszenia o statusie PENDING"))
            }

            querySnapshot.documents.first().reference.update("status", "ACCEPTED").await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}