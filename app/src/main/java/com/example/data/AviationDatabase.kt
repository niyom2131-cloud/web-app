package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AviationDao {
    @Query("SELECT * FROM aviation_records ORDER BY savedAt DESC")
    fun getAllRecords(): Flow<List<AviationRecord>>

    @Query("SELECT * FROM aviation_records WHERE type = :type ORDER BY savedAt DESC")
    fun getRecordsByType(type: String): Flow<List<AviationRecord>>

    @Query("SELECT * FROM aviation_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Int): AviationRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AviationRecord): Long

    @Query("DELETE FROM aviation_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM aviation_records")
    suspend fun clearAllRecords()
}

@Database(entities = [AviationRecord::class], version = 1, exportSchema = false)
abstract class AviationDatabase : RoomDatabase() {
    abstract fun dao(): AviationDao

    companion object {
        @Volatile
        private var INSTANCE: AviationDatabase? = null

        fun getDatabase(context: Context): AviationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AviationDatabase::class.java,
                    "aviation_maintenance_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class AviationRepository(private val dao: AviationDao) {
    val allRecords: Flow<List<AviationRecord>> = dao.getAllRecords()

    fun getRecordsByType(type: String): Flow<List<AviationRecord>> = dao.getRecordsByType(type)

    suspend fun getRecordById(id: Int): AviationRecord? = dao.getRecordById(id)

    suspend fun insertRecord(record: AviationRecord): Long = dao.insertRecord(record)

    suspend fun deleteRecordById(id: Int) = dao.deleteRecordById(id)

    suspend fun clearAllRecords() = dao.clearAllRecords()
}
