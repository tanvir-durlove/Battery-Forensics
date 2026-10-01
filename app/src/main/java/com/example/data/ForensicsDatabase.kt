package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ForensicsDao {
    @Query("SELECT * FROM diagnostic_sessions ORDER BY timestamp DESC")
    fun getAllDiagnosticSessions(): Flow<List<DiagnosticSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosticSessions(sessions: List<DiagnosticSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosticSession(session: DiagnosticSessionEntity)

    @Query("DELETE FROM diagnostic_sessions WHERE id = :id")
    suspend fun deleteDiagnosticSession(id: String)

    @Query("SELECT * FROM timeline_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getTimelineEventsForSession(sessionId: String): Flow<List<TimelineEventEntity>>

    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC")
    fun getAllTimelineEvents(): Flow<List<TimelineEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvents(events: List<TimelineEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: TimelineEventEntity)

    @Query("SELECT * FROM charging_sessions ORDER BY timestamp DESC")
    fun getAllChargingSessions(): Flow<List<ChargingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargingSessions(sessions: List<ChargingSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargingSession(session: ChargingSessionEntity)

    @Query("SELECT * FROM charger_profiles ORDER BY maxObservedWatts DESC")
    fun getAllChargerProfiles(): Flow<List<ChargerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargerProfiles(profiles: List<ChargerProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargerProfile(profile: ChargerProfileEntity)

    @Query("SELECT * FROM controlled_experiments ORDER BY createdAt DESC")
    fun getAllExperiments(): Flow<List<ExperimentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiments(experiments: List<ExperimentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: ExperimentEntity)

    @Query("DELETE FROM diagnostic_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM timeline_events")
    suspend fun clearAllTimelineEvents()

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAllChargingSessions()

    @Query("DELETE FROM charger_profiles")
    suspend fun clearAllChargerProfiles()

    @Query("DELETE FROM controlled_experiments")
    suspend fun clearAllExperiments()
}

@Database(
    entities = [
        DiagnosticSessionEntity::class,
        TimelineEventEntity::class,
        ChargingSessionEntity::class,
        ChargerProfileEntity::class,
        ExperimentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ForensicsDatabase : RoomDatabase() {
    abstract fun forensicsDao(): ForensicsDao

    companion object {
        @Volatile
        private var INSTANCE: ForensicsDatabase? = null

        fun getInstance(context: Context): ForensicsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForensicsDatabase::class.java,
                    "battery_forensics_encrypted_local.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
