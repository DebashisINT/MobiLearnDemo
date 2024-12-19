package com.breezemobilearndemo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class NotificationUtils(headerText: String, bodyText: String, shopId: String, localShopId: String) {


    companion object {
        fun cancelNotification(id: Int, tag: String, mContext: Context) {
            //you can get notificationManager like this:
            val notificationmanager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationmanager.cancel(tag, id)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendFCMNotificaiton(applicationContext: Context, remoteMessage: RemoteMessage?) {

        val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000

        val notificationmanager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val remoteView = RemoteViews(applicationContext.packageName, R.layout.customnotificationsmall)

        remoteView.setImageViewResource(R.id.imagenotileft_small, R.drawable.breezelogo)
        remoteView.setTextViewText(R.id.title_small, remoteMessage?.data?.get("body"))
        remoteView.setTextViewText(R.id.text_small, "Nordusk")

        val shopIntent = Intent(applicationContext, DashboardActivity::class.java)

        if(!TextUtils.isEmpty(remoteMessage?.data?.get("type")) && remoteMessage?.data?.get("type") == "lms_content_assign")
            shopIntent.putExtra("TYPE", "lms_content_assign")
        else
            shopIntent.putExtra("TYPE", "PUSH")
        shopIntent.action = Intent.ACTION_MAIN
        shopIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, shopIntent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = AppUtils.notificationChannelId

            val channelName = AppUtils.notificationChannelName

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationmanager.createNotificationChannel(notificationChannel)

            val notificationBuilder = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.breezelogo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setChannelId(channelId)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setGroup("Lms Group")
                .setGroupSummary(true)
                .setContentText(remoteMessage?.data?.get("body").toString())
                .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage?.data?.get("body").toString()))
                .build()

            notificationmanager.notify(m, notificationBuilder)
        } else {
            val notification = NotificationCompat.Builder(
                applicationContext)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notifications_icon)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setGroup("Lms Group")
                .setGroupSummary(true)
                .setContent(remoteView)
                .build()

            notificationmanager.notify(m, notification)
        }

    }
}