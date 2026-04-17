package com.example.smartmedication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smartmedication.data.AppDatabase
import com.example.smartmedication.worker.MedicationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val dao = AppDatabase.getDatabase(context).medicationDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get all medications that haven't been taken
                    val pendingMeds = dao.getAllUntakenMedications()
                    val workManager = WorkManager.getInstance(context)

                    for (med in pendingMeds) {
                        val currentTime = System.currentTimeMillis()
                        val delay = med.targetTimeInMillis - currentTime
                        
                        val finalDelay = if (delay > 0) delay else 0L

                        val inputData = Data.Builder()
                            .putInt("MED_ID", med.id)
                            .putString("MED_NAME", med.name)
                            .putString("MED_DOSAGE", med.dosage)
                            .build()

                        // Removed battery constraints to ensure it fires even if battery is low
                        val workRequest = OneTimeWorkRequestBuilder<MedicationWorker>()
                            .setInitialDelay(finalDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .build()

                        workManager.enqueueUniqueWork(
                            "Med_${med.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
