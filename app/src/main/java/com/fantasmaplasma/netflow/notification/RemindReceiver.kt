package com.fantasmaplasma.netflow.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fantasmaplasma.netflow.R
import com.fantasmaplasma.netflow.ui.MainActivity

class RemindReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val builder = getNotification(context)
        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, builder.build())
        sendNotificationTomorrow(context)
    }

    private fun getNotification(context: Context) : NotificationCompat.Builder {
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.reminder))
                .setContentText(context.getString(R.string.notification_body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setNotificationSilent()
    }

    private fun sendNotificationTomorrow(context: Context) {
        //Set notification to go off a day from when this is sent.
        val remind = Intent(context, RemindReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, remind, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val oneDay: Long = 86400000
        alarmManager[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + oneDay] = pendingIntent
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }
}