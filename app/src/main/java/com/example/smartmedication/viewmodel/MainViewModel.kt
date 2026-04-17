package com.example.smartmedication.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.smartmedication.data.AppDatabase
import com.example.smartmedication.data.Medication
import com.example.smartmedication.worker.MedicationWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).medicationDao()
    private val workManager = WorkManager.getInstance(application)

    // UI State for Form - Uses Compose State for real-time UI updates
    var name by mutableStateOf("")
    var dosage by mutableStateOf("")
    var frequency by mutableStateOf("Daily")
    var selectedTimeText by mutableStateOf("Select Time")

    // Persistent List from Room
    val medications = dao.getAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearFields() {
        name = ""
        dosage = ""
        selectedTimeText = "Select Time"
    }

    fun addMedication(name: String, dosage: String, frequency: String, targetTime: Long) {
        viewModelScope.launch {
            val newMed = Medication(
                name = name,
                dosage = dosage,
                frequency = frequency,
                targetTimeInMillis = targetTime
            )
            val insertedId = dao.insert(newMed).toInt()
            scheduleNotification(insertedId, name, dosage, targetTime)

            clearFields()
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            dao.update(medication)
            scheduleNotification(medication.id, medication.name, medication.dosage, medication.targetTimeInMillis)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            dao.delete(medication)
            workManager.cancelUniqueWork("Med_${medication.id}")
        }
    }

    private fun scheduleNotification(id: Int, name: String, dosage: String, targetTime: Long) {
        val delay = targetTime - System.currentTimeMillis()

        // If delay is negative, it means the time has passed. 
        // We should trigger it immediately (delay = 0) to ensure the user doesn't miss it.
        val finalDelay = if (delay > 0) delay else 0L

        val inputData = workDataOf(
            "MED_ID" to id,
            "MED_NAME" to name,
            "MED_DOSAGE" to dosage
        )

        // Removed battery constraints to ensure reliability for medication reminders
        val request = OneTimeWorkRequestBuilder<MedicationWorker>()
            .setInitialDelay(finalDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork("Med_$id", ExistingWorkPolicy.REPLACE, request)
    }
}
