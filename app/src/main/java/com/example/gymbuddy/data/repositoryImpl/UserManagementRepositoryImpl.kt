package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.data.authentication.UserInformation
import com.example.gymbuddy.repository.UserManagementRepository
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class UserManagementRepositoryImpl : UserManagementRepository {

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
                .set(
                    newUserData,
                    SetOptions.merge()
                ) // Merge powoduje aktualizację tylko określonych pól
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
                    println("Username already taken!")
                    throw Exception("Username already taken")
                } else {
                    transaction.set(
                        docRef,
                        emptyMap<String, Any>()
                    ) // jesli dokument nie istnieje to zapisujemy do niego pusta mape, czyli po prostu tworzymy pusty dokument
                }
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
            Result.success(true) // nawet jesli dokument jaki chcemy usunac nie bedzie istnial to i tak zwrocony bedzie sukces
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // search screen
    override suspend fun searchUser(username: String): Result<UserFoundInformation> {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull() ?: return Result.failure(
                NoSuchElementException("No user Found: $username"))

            val user = document.toObject(UserFoundInformation::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Error while converting to UserFoundInformation"))
            }
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

    // zeby usunac usera trzeba:
    // - usunac z db +
    // - usunac jego nickaname +
    // - usunac jego zdjecie profilowe z db +
    // - usunac wszysktie jego wiadomosci
    // - usunac wszystkie jego przyjazni i friends requesty todo

    // usuwanie z db
    override suspend fun deleteUser(userId: String): Result<Boolean> {
        return try {
            db.collection("users")
                .document(userId)
                .delete()
                .await()

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