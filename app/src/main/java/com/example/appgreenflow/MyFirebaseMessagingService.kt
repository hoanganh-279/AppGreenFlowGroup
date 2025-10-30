package com.example.appgreenflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: " + remoteMessage.getFrom())

        if (remoteMessage.getData().size > 0) {
            val type = remoteMessage.getData().get("type")
            val message = remoteMessage.getData().get("message")

            if ("new_report" == type) {
                showNotification("Báo cáo mới", message)
            }
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Reports", NotificationManager.IMPORTANCE_HIGH)
            channel.setDescription("Thông báo báo cáo lỗi")
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token: " + token)
        FirebaseMessaging.getInstance().subscribeToTopic("employee")
            .addOnCompleteListener(OnCompleteListener { task: Task<Void?>? ->
                if (task!!.isSuccessful()) {
                    Log.d(TAG, "Subscribed to employee topic")
                }
            })
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "reports_channel"
    }
}