package com.example.smartmedication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications ORDER BY targetTimeInMillis ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE isTaken = 0")
    suspend fun getAllUntakenMedications(): List<Medication>

    @Query("SELECT * FROM medications WHERE targetTimeInMillis > :currentTime AND isTaken = 0")
    suspend fun getPendingMedications(currentTime: Long): List<Medication>

    @Query("UPDATE medications SET isTaken = 1 WHERE id = :medId")
    suspend fun markAsTaken(medId: Int)
}
