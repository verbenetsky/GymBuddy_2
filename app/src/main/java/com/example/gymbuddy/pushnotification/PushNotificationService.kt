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
            .addOnFailureListener { e ->
                println("Błąd przy aktualizacji tokena FCM")
            }
    }

    // respond to received messages
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Jeśli aplikacja jest w pierwszym planie, możemy wyświetlić powiadomienie ręcznie.
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Default Title"
            val body = notification.body ?: "Default Body"
            sendNotification(title, body)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(title: String, body: String) {
        // Utwórz kanał powiadomień, jeśli jeszcze nie został utworzony (dla API 26+)
        val channelId = "default_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Default Channel"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Buduj powiadomienie
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_gymbuddy_black_theme)  // upewnij się, że masz ikonę w zasobach
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Wyświetl powiadomienie
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(/*notification id*/ 0, notificationBuilder.build())
    }
}