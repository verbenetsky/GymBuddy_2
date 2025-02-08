package com.example.gymbuddy.data.repository

import com.example.gymbuddy.repository.CloudStorageRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CloudStorageRepositoryImpl : CloudStorageRepository {

    private val storageRef = Firebase.storage.reference
    private val user = Firebase.auth.currentUser
    private val userId = user?.uid

    override suspend fun uploadImage(imageUri: android.net.Uri): Result<Boolean> {

        return try {
            val localTime = System.currentTimeMillis()
            val riversRef = storageRef.child("images/${localTime}_${userId}")
            riversRef.putFile(imageUri)
                .addOnFailureListener {
                    println("Upload failed: ${it.message}")
                }
                .addOnSuccessListener {
                    println("Upload succeeded!")
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(imageUri: android.net.Uri): Result<Boolean> {
        TODO("Not yet implemented")
    }


}