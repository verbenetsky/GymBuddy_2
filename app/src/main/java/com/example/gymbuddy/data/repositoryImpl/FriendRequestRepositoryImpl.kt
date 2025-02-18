package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.friends.FriendRequestInformationDto
import com.example.gymbuddy.repository.FriendRequestRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FriendRequestRepositoryImpl : FriendRequestRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override suspend fun acceptFriendRequest(
        senderId: String,
        receiverId: String
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun declineFriendRequest(
        senderId: String,
        receiverId: String
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getFriendRequests(userId: String): Result<List<FriendRequestInformationDto>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFriends(userId: String): Result<List<String>> {
        TODO("Not yet implemented")
    }
}