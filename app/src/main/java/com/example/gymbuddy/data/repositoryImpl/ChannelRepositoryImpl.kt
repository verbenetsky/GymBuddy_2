package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.data.model.Channel
import com.example.gymbuddy.data.model.UserFoundInformation
import com.example.gymbuddy.data.repository.ChannelRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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

    override suspend fun findChannel(friendId: String, currentUserId: String): Result<String> {
        return try {
            val snapshot = db.collection("channels")
                .whereEqualTo("firstFriendId", friendId)
                .whereEqualTo("secondFriendId", currentUserId)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.getString("id")?.let { id ->
                return Result.success(id)
            }

            val snapshot1 = db.collection("channels")
                .whereEqualTo("firstFriendId", currentUserId)
                .whereEqualTo("secondFriendId", friendId)
                .get()
                .await()

            snapshot1.documents.firstOrNull()?.getString("id")?.let { id ->
                return Result.success(id)
            }


            Result.success("")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override suspend fun deleteAllChannels(currentUserId: String): Result<Boolean> {
        return try {
            while (true) {
                val snapshot: QuerySnapshot =
                    db.collection("channels")
                        .whereEqualTo("secondFriendId", currentUserId)
                        .limit(500)
                        .get()
                        .await()

                if (snapshot.isEmpty) {
                    break
                }

                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }

            while (true) {
                val snapshot1: QuerySnapshot =
                    db.collection("channels")
                        .whereEqualTo("firstFriendId", currentUserId)
                        .limit(500)
                        .get()
                        .await()

                if (snapshot1.isEmpty) {
                    break
                }

                val batch1 = db.batch()
                snapshot1.documents.forEach { batch1.delete(it.reference) }
                batch1.commit().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

        awaitClose {
            l1.remove()
            l2.remove()
        }
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
                .document(channel.id)
                .set(channel)
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