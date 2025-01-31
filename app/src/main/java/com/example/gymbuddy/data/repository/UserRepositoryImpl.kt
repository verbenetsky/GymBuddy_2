package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.authentication.UserInformation
import com.example.gymbuddy.data.authentication.UserInformationViewModel
import com.example.gymbuddy.repository.DatabaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository() : DatabaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override suspend fun addUser(userInformation: UserInformation): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userInformation.userId)
                .set(userInformation)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userId)
                .delete()
                .await()

            val user = auth.currentUser
            user?.delete()?.await() ?: throw Exception("User not found")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<UserInformation> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val userInfo = document.toObject(UserInformation::class.java)
                userInfo?.let { Result.success(it) } ?: Result.failure(Exception("There is no data for this user"))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
}