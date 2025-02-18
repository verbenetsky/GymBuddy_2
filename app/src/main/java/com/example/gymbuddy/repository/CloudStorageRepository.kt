package com.example.gymbuddy.repository

interface CloudStorageRepository {
    suspend fun uploadImage(imageUri: android.net.Uri): Result<String>
    suspend fun deleteImage(imageUri: String): Result<Boolean>
}