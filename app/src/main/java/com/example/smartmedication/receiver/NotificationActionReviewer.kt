package com.example.smartmedication.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.smartmedication.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getIntExtra("MED_ID", -1)
        
        if (medId != -1) {
            // 1. DISMISS NOTIFICATION IMMEDIATELY
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(medId)

            WorkManager.getInstance(context).cancelUniqueWork("Med_$medId")

            // 3. UPDATE DATABASE IN BACKGROUND
            val dao = AppDatabase.getDatabase(context).medicationDao()
            val pendingResult = goAsync() // Allows the receiver to stay alive for the coroutine
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    dao.markAsTaken(medId)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
