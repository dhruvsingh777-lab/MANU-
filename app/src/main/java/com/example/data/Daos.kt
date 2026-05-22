package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // Exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE category = :category")
    fun getExercisesByCategoryFlow(category: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    // Workout Plans
    @Query("SELECT * FROM workout_plans ORDER BY id ASC")
    fun getWorkoutPlanItemFlow(): Flow<List<WorkoutPlanItem>>

    @Query("SELECT * FROM workout_plans")
    suspend fun getWorkoutPlanItems(): List<WorkoutPlanItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlanItems(items: List<WorkoutPlanItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlanItem(item: WorkoutPlanItem)

    @Update
    suspend fun updateWorkoutPlanItem(item: WorkoutPlanItem)

    @Query("DELETE FROM workout_plans")
    suspend fun clearWorkoutPlan()

    // Diet Logs
    @Query("SELECT * FROM diet_logs WHERE dateString = :dateString ORDER BY id ASC")
    fun getDietLogsByDateFlow(dateString: String): Flow<List<DietLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(log: DietLog)

    @Delete
    suspend fun deleteDietLog(log: DietLog)

    // Water Logs
    @Query("SELECT * FROM water_logs WHERE dateString = :dateString")
    fun getWaterLogByDateFlow(dateString: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    // Chat Logs
    @Query("SELECT * FROM chat_logs ORDER BY timestamp ASC")
    fun getChatLogsFlow(): Flow<List<ChatLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatLog(chat: ChatLog)

    @Query("DELETE FROM chat_logs")
    suspend fun clearChatLogs()

    // Progress History
    @Query("SELECT * FROM progress_history ORDER BY timestamp ASC")
    fun getProgressHistoryFlow(): Flow<List<ProgressHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressHistory(progress: ProgressHistory)

    // Community Posts
    @Query("SELECT * FROM community_posts ORDER BY id DESC")
    fun getCommunityPostsFlow(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPost(post: CommunityPost)

    @Update
    suspend fun updateCommunityPost(post: CommunityPost)
}
