package com.example.gymbuddy.data.repositoryImpl

import android.net.Uri
import com.example.gymbuddy.data.repository.CloudStorageRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CloudStorageRepositoryImpl @Inject constructor() : CloudStorageRepository {

    private val storageRef = Firebase.storage.reference
    private val db = Firebase.firestore

    override suspend fun uploadImage(imageUri: Uri, userId: String): Result<String> {
        return try {
            val localTime = System.currentTimeMillis()
            val imagePath = "images/${localTime}_${userId}"
            val riversRef = storageRef.child(imagePath)

            riversRef.putFile(imageUri).await()

            val downloadUrl = riversRef.downloadUrl.await().toString()
            println("Upload succeeded! Download URL: $downloadUrl")

            db.collection("users").document(userId)
                .update("profilePictureUrl", downloadUrl)
                .await()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            println("Upload failed: ${e.message}")
            Result.failure(e)
        }
    }



    override suspend fun deleteImage(imageUri: String): Result<Boolean> {
        return try {
            val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri)
            fileRef.delete().await()
            println("Delete succeeded!")
            Result.success(true)
        } catch (e: Exception) {
            println("Delete failed: $e")
            Result.failure(e)
        }
    }
}