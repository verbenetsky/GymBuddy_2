package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.buttonState.ButtonStateManager
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto
import com.example.gymbuddy.repository.FriendRequestRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FriendRequestRepositoryImpl : FriendRequestRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override suspend fun addFriendRequestToDataBase(
        friendRequest: FriendRequestInformationDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ): Result<Boolean> {
        return try {
            db.collection("users") //kolekcja userow
                .document(friendRequest.receiverId) // zeby dodac do bazy danych receivera
                .collection("friendRequests")
                .document(friendRequest.senderId) // nazwa dokumnetu jest id sendera
                .set(friendRequest)
                .await()
            onSuccess()
            Result.success(true)
        } catch (e: Exception) {
            onFailure()
            Result.failure(e)
        }
    }

    override suspend fun getAllFullFriendsRequestInformation(listOfFriendRequest: List<FriendRequestInformationDto>): Result<List<UserFoundInformation>> {
        return try {
            val listOfUsers = coroutineScope { // asynchronicznie to robimy zeby czasu nie tracic
                listOfFriendRequest.map { friendRequest ->
                    async {
                        val snapshot = db.collection("users")
                            .document(friendRequest.senderId)
                            .get()
                            .await()
                        snapshot.toObject(UserFoundInformation::class.java)
                    }
                }

            }.awaitAll().filterNotNull() // czekamy wszystkie pobierzemy z bazy danych
            Result.success(listOfUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllFriend(currentUserId: String): Result<List<UserFoundInformation>> {
        return try {
            // pobieramy liste wszyskich userId zeby potem na ich podstawie pobrac wszystkich userow
            val listOfFriendsId = mutableListOf<String>()
            val snapshot = db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()
            snapshot.documents.forEach { document ->
                listOfFriendsId.add(document.id)
            }
            val listOfUsers = coroutineScope {
                listOfFriendsId.map { friendId ->
                    async {
                        val snapshot1 = db.collection("users")
                            .document(friendId)
                            .get()
                            .await()
                        snapshot1.toObject(UserFoundInformation::class.java)
                    }
                }
            }.awaitAll().filterNotNull()
            Result.success(listOfUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Boolean> {
        return try {
            coroutineScope {
                val job1 = async {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("friends")
                        .document(friendId)
                        .delete()
                }

                val job2 = async {
                    db.collection("users")
                        .document(friendId)
                        .collection("friends")
                        .document(currentUserId)
                        .delete()
                }
                awaitAll(job1, job2)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkFriendShip(friendId: String): Result<Boolean> {
        return try {
            val snapshot = db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("friends")
                .document(friendId)
                .get()
                .await()
            println(snapshot)
            if (snapshot.exists()) {
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllFriendRequests(currentUserId: String): Result<List<FriendRequestInformationDto>> {
        return try {
            val snapshot =
                db.collection("users")
                    .document(currentUserId)
                    .collection("friendRequests")
                    .get()
                    .await()

            val friendRequests = snapshot.documents.mapNotNull { document ->
                document.toObject(FriendRequestInformationDto::class.java)
            }

            Result.success(friendRequests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // sprawdzamy w jakiej relacji jestemsy z konkretnym userem moga byc takie opcje:
    // 1. Zadna relacja - Send Request
    // 2. wyslalismy request - Request Sent
    // 3. Znajomi - Remove
    // dodatkowo jesli ktos nam wyslal request to moze byc tak:
    // 1. Zadna relacja - Send Request
    // 2. ktos wyslal request - Decline
    // 3. Znajomi - Remove


    override suspend fun determineButtonState(searchedUserId: String): Result<String> {
        return try {
            val currentUserId = Firebase.auth.currentUser!!.uid

            val sentRequestSnapshot = db.collection("users")
                .document(searchedUserId)
                .collection("friendRequests")
                .document(currentUserId)
                .get()
                .await()

            val receivedRequestSnapshot = db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(searchedUserId)
                .get()
                .await()

            val friendSnapshot = db.collection("users")
                .document(searchedUserId)
                .collection("friends")
                .document(currentUserId)
                .get()
                .await()

            val state = when {
                friendSnapshot.exists() -> "Remove"
                sentRequestSnapshot.exists() -> "Request Sent"
                receivedRequestSnapshot.exists() -> "Decline"
                else -> "Send Request"
            }

            Result.success(state)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // tutaj to dodaje instancje FriendInformation data class do kolekcji friends, czyli dodajemy do znajomych
    override suspend fun addFriendsInformationToDatabase(
        currentUserId: String,
        friendId: String
    ): Result<Boolean> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val current = LocalDateTime.now().format(formatter)
        return try {
            coroutineScope {
                val jobCurrentUser = async {
                    val dataToAddForCurrentUser = mapOf(
                        "friendId" to friendId,
                        "dateOfCreationOfFriendShip" to current
                    )
                    db.collection("users")
                        .document(currentUserId)
                        .collection("friends")
                        .document(friendId)
                        .set(dataToAddForCurrentUser)
                        .await()
                }
                val jobFriendUser = async {
                    val dataToAddForFriendUser = mapOf(
                        "friendId" to currentUserId,
                        "dateOfCreationOfFriendShip" to current
                    )
                    db.collection("users")
                        .document(friendId)
                        .collection("friends")
                        .document(currentUserId)
                        .set(dataToAddForFriendUser)
                        .await()
                }
                jobFriendUser.await()
                jobCurrentUser.await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Decline
    override suspend fun deleteFriendRequestAfterAcceptingOrDecliningRequest(
        currentUserId: String,
        friendId: String
    ): Result<Boolean> {
        return try {
            db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(friendId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}