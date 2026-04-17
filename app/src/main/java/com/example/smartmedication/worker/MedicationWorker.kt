package com.example.smartmedication.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.smartmedication.MainActivity
import com.example.smartmedication.receiver.NotificationActionReceiver

class MedicationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    //WorkManager
    override fun doWork(): Result {
        val medId = inputData.getInt("MED_ID", -1)
        val medName = inputData.getString("MED_NAME") ?: return Result.failure()
        val medDosage = inputData.getString("MED_DOSAGE") ?: return Result.failure()

        showNotification(medId, medName, medDosage)
        return Result.success()
    }

    private fun showNotification(medId: Int, name: String, dosage: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_urgent_v5"
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            //Notification Channel
            val channel = NotificationChannel(channelId, "Medication Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Urgent medication reminders"
                setSound(alarmSound, audioAttributes)
                enableVibration(true)

                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action for "Mark as Taken"
        val actionIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            putExtra("MED_ID", medId)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            applicationContext, medId, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(applicationContext, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(name)
            .setContentText("$dosage - Time to take your meds!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .addAction(0, "Mark as Taken", actionPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true) // Keeps the notification from being swiped away
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notification = notificationBuilder.build()
        // FLAG_INSISTENT makes the sound loop until the notification is cancelled
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(medId, notification)
    }
}
