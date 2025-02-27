package com.example.gymbuddy.channel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor() : ViewModel() {

    private val db = Firebase.firestore

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannels()
    }

    private fun getChannels() {
        db.collection("channels")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val channels = mutableListOf<Channel>()
                for (document in querySnapshot) {
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                    channels.add(Channel(id, name, createdAt))
                }
                _channels.value = channels
                println("channels: ")
                println(_channels.value)
            }
            .addOnFailureListener { e ->
                println("Error fetching channels $e")
            }
    }

    fun addChannel(name: String) {
        val newChannel = Channel(name = name)
        db.collection("channels")
            .add(newChannel)
            .addOnSuccessListener { documentReference -> // documentReference to jest sam obiekt
                db.collection("channels")
                    .document(documentReference.id)
                    .update("id", documentReference.id)
                    .addOnSuccessListener {
                        getChannels()
                    }
            }
            .addOnFailureListener { e ->
                println("Error adding channel $e")
            }
    }
}