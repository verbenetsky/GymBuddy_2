package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = Firebase.auth

    override suspend fun logIn(email: String, password: String): Boolean {
        // Jeśli coś pójdzie nie tak, wyjątek zostanie rzucony i nie złapany tutaj.
        auth.signInWithEmailAndPassword(email, password).await()
        return true
    }

    // synchroniczne dzialanie ale mozna uzyc suspend dla spojnosci
    override suspend fun logOut(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid ?: throw Exception("User ID is null")
        return true
    }

    override suspend fun signInWithCredentials(token: String): Boolean {
        val credentials = GoogleAuthProvider.getCredential(token, null)
        val result = auth.signInWithCredential(credentials).await()

        val isNew = result.additionalUserInfo?.isNewUser ?: false

        result.user ?: throw IllegalStateException("Firebase zwrócił pustego użytkownika")

        return isNew
    }

    override suspend fun deleteUserAccount(): Result<Boolean> {
        return try {
            val currentUser =
                auth.currentUser ?: return Result.failure(Exception("Nobody is logged."))
            currentUser.delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendResetPasswordEmail(email: String): Boolean {
        auth.sendPasswordResetEmail(email).await()
        return true
    }
}