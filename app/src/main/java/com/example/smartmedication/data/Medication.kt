package com.example.smartmedication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val targetTimeInMillis: Long,
    val isTaken: Boolean = false
)