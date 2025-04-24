package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.channel.Channel
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.repository.ChannelRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ChannelRepository {

    override suspend fun deleteChannel(channelId: String): Result<Boolean> {
        return try {
            db.collection("channels")
                .document(channelId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    override fun observeLastMessage(channelId: String): Flow<String> = callbackFlow {
//        val registration = db.collection("channels")
//            .document(channelId)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    return@addSnapshotListener
//                } else {
//                    val lastMsg = snapshot?.getString("lastMessage")
//                    if (lastMsg != null) {
//                        trySend(lastMsg).isSuccess
//                    }
//                }
//            }
//        awaitClose { registration.remove() }
//    }

    override suspend fun getLastMessage(channelId: String): Result<String> {
        return try {
            val snapshot = db.collection("channels")
                .document(channelId)
                .get()
                .await()

            Result.success(snapshot.getString("lastMessage")!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeChannelsForUser(userId: String): Flow<Set<Channel>> = callbackFlow {
        var firstSide = emptyList<Channel>()
        var secondSide = emptyList<Channel>()

        fun sendMerged() = trySend((firstSide + secondSide).toSet())

        val l1 = db.collection("channels")
            .whereEqualTo("firstFriendId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                firstSide = snap?.documents?.map { it.toChannel() } ?: emptyList()
                sendMerged()
            }

        val l2 = db.collection("channels")
            .whereEqualTo("secondFriendId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                secondSide = snap?.documents?.map { it.toChannel() } ?: emptyList()
                sendMerged()
            }

        awaitClose { l1.remove(); l2.remove() }
    }


    override fun computeFriendsWithoutChannel(
        allFriends: List<UserFoundInformation>,
        existingChannels: Set<Channel>,
        currentUserId: String
    ): Set<UserFoundInformation> {
        val friendsInChannels = existingChannels
            .map { channel ->
                if (channel.firstFriendId == currentUserId)
                    channel.secondFriendId
                else
                    channel.firstFriendId
            }
            .toSet()

        return allFriends
            .filterNot { it.userId in friendsInChannels }
            .toSet()
    }


    override suspend fun addChannel(channel: Channel): Result<Boolean> {
        return try {
            db.collection("channels")
                .add(channel)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toChannel() = Channel(
        id = id,
        firstFriendId = getString("firstFriendId").orEmpty(),
        secondFriendId = getString("secondFriendId").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L
    )
}