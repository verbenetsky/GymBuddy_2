package com.example.gymbuddy.repository

import coil3.Uri

interface CloudStorageRepository {
    suspend fun uploadImage(imageUri: android.net.Uri): Result<Boolean>

    suspend fun deleteImage(imageUri: android.net.Uri): Result<Boolean>
}