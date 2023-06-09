package com.antonin.friendswave.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.antonin.friendswave.ui.event.RatingActivity
import com.antonin.friendswave.ui.fragmentMain.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.R

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Le lien avec la base de données Firebase pour envoyer des notifications sur un ou des téléphones.

//Documentation https://firebase.google.com/docs/cloud-messaging/android/client?hl=fr

class FirebaseNotificationsService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"]!!.toString()
        val body = message.data["body"]!!.toString()
        val intent_action = message.data["click_action"].toString()
        sendNotification(title,body, intent_action)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendNotification(titre: String, body:String, action: String) {

        var intent = Intent()

        if(action == "OPEN_ACTIVITY") {
            intent = Intent(this, RatingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        if(action != "OPEN_ACTIVITY") {
            intent = Intent(this, HomeFragment::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }


        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(titre)
            .setContentText(body)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.notification_bg)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sendRegistrationToServer(token)

    }
    fun sendRegistrationToServer(token: String) {

        val reference = FirebaseDatabase.getInstance().reference
        reference.child("user").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("token")
            .setValue(token)
    }
}