package com.example.gymbuddy.pushnotification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gymbuddy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {
    // update server
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("token has been updated")
        println(token)
        updateFcmToken(token)
    }

    private fun updateFcmToken(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("PushNotificationService", "User is not authenticated")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUser.uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                println("Token FCM został zaktualizowany")
                println(token)
            }
            .addOnFailureListener {
                println("Błąd przy aktualizacji tokena FCM")
            }
    }

    // respond to received messages todo
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

    }

}