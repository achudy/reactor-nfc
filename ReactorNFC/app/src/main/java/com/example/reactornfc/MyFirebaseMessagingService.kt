package com.example.reactornfc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE


/**
 * A generic Firebase messaging service.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Message receival override.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val CHANNEL_ID = "com.example.reactornfc.channel_id"

        /**
         * Fail on unsupported versions.
         */
        if (Build.VERSION.SDK_INT >= 26) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_HIGH)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }

        /**
         * Making the Intent that should spawn from the notification.
         */
        val notificationIntent = Intent("android.intent.category.LAUNCHER")
        notificationIntent.setClassName(
            "com.example.reactornfc",
            "com.example.reactornfc.MainActivity"
        )
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        /**
         * Activity for the pending intent.
         */
        val contentIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)

        /**
         * Building the notification itself.
         */
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PRIVATE)
            .setFullScreenIntent(contentIntent, true)
            .build()
        NotificationManagerCompat.from(this).notify(R.string.notification_id, notification)
    }
}
