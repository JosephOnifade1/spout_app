package com.example.data.local

import androidx.room.*
import com.example.data.CheckIn
import com.example.data.Converters
import com.example.data.Habit
import com.example.data.Milestone
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<Habit>

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun getHabitById(habitId: String): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE habitId = :habitId")
    suspend fun deleteHabitById(habitId: String)

    @Query("SELECT * FROM check_ins")
    fun getAllCheckIns(): Flow<List<CheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn)

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckIn)

    @Query("DELETE FROM check_ins WHERE checkInId = :checkInId")
    suspend fun deleteCheckInById(checkInId: String)

    @Query("SELECT * FROM milestones ORDER BY dateReached DESC")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Query("DELETE FROM milestones WHERE habitId = :habitId")
    suspend fun deleteMilestonesByHabitId(habitId: String)
}

@Database(entities = [Habit::class, CheckIn::class, Milestone::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PerennialDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: PerennialDatabase? = null

        fun getDatabase(context: android.content.Context): PerennialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PerennialDatabase::class.java,
                    "perennial_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
