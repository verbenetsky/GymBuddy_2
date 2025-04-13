package com.example.gymbuddy.repository

interface CloudStorageRepository {
    suspend fun uploadImage(imageUri: android.net.Uri, userId: String): Result<String>
    suspend fun deleteImage(imageUri: String): Result<Boolean>
}