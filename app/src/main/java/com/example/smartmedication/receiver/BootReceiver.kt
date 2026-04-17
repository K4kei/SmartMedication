package com.example.smartmedication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
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
            val dao = AppDatabase.getDatabase(context).medicationDao()

            CoroutineScope(Dispatchers.IO).launch {
                val pendingMeds = dao.getPendingMedications(System.currentTimeMillis())
                val workManager = WorkManager.getInstance(context)

                for (med in pendingMeds) {
                    val delay = med.targetTimeInMillis - System.currentTimeMillis()
                    if (delay > 0) {
                        val inputData = Data.Builder()
                            .putInt("MED_ID", med.id)
                            .putString("MED_NAME", med.name)
                            .putString("MED_DOSAGE", med.dosage)
                            .build()

                        // power-sensitive development constraints
                        val constraints = Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()

                        val workRequest = OneTimeWorkRequestBuilder<MedicationWorker>()
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setConstraints(constraints)
                            .setInputData(inputData)
                            .build()

                        workManager.enqueueUniqueWork(
                            "Med_${med.id}",
                            ExistingWorkPolicy.REPLACE,
                            workRequest
                        )
                    }
                }
            }
        }
    }
}