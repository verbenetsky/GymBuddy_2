package com.example.gymbuddy.repository

interface DatabaseRepositoryImpl {
    suspend fun addUser(): Result<Boolean>
}