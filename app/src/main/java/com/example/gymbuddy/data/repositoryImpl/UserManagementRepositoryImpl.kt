package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserInformation
import com.example.gymbuddy.repository.UserManagementRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class UserManagementRepositoryImpl : UserManagementRepository {

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

    override suspend fun getUserFromFireStoreToViewModel(userId: String): Result<UserInformation> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()
            if (document.exists()) {
                val userInformation = document.toObject(UserInformation::class.java)
                if (userInformation != null) {
                    Result.success(userInformation)
                } else {
                    Result.failure(Exception("User information is null"))
                }
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(newUserData: UserInformation, userId: String): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userId)
                .set(newUserData)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addUsernameToDataBase(username: String): Result<Boolean> {
        return try {
            db.runTransaction { transaction ->
                val docRef = db.collection("usernames").document(username)
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    throw Exception("Username already taken")
                }
                transaction.set(
                    docRef,
                    emptyMap<String, Any>()
                ) // jesli dokument nie istnieje to zapisujemy do niego pusta mape, czyli po prostu tworzymy pusty dokument
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUsernameFromDataBase(username: String): Result<Boolean> {
        return try {
            db.collection("usernames")
                .document(username)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUser(username: String): Result<List<UserFoundInformation>> {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            Result.success(querySnapshot.toObjects(UserFoundInformation::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFcmTokenToDataBase(
        userId: String,
        token: String,
    ): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFcmToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Result.success(token)
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

    override suspend fun getUser(userId: String): Result<UserFoundInformation> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()
            if (document.exists()) {
                val userInfo = document.toObject(UserFoundInformation::class.java)
                userInfo?.let {
                    Result.success(it)
                }
                    ?: Result.failure(Exception("There is no data for this user"))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}