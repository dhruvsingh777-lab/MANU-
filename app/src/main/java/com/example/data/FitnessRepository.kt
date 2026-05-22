package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class FitnessRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val fitnessDao = db.fitnessDao()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // User Profile
    val userProfile: Flow<UserProfile?> = fitnessDao.getUserProfileFlow()

    suspend fun saveUserProfile(profile: UserProfile) {
        fitnessDao.insertUserProfile(profile)
    }

    suspend fun addXpAndCheckStreak(xpToAdd: Int) {
        val currentProfile = fitnessDao.getUserProfile() ?: UserProfile()
        val newXp = currentProfile.xpPoints + xpToAdd
        
        // Simple streak calculation: if last update was yesterday, streak increments. If today, same. If longer, reset.
        val today = getTodayDateString()
        val lastUpdateDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentProfile.lastStreakUpdate))
        
        var newStreak = currentProfile.dailyStreak
        if (today != lastUpdateDay) {
            val diffMs = System.currentTimeMillis() - currentProfile.lastStreakUpdate
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            newStreak = if (diffDays <= 1) {
                currentProfile.dailyStreak + 1
            } else {
                1
            }
        }

        fitnessDao.insertUserProfile(
            currentProfile.copy(
                xpPoints = newXp,
                dailyStreak = newStreak,
                lastStreakUpdate = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSubscription(isPremium: Boolean, subType: String) {
        val p = fitnessDao.getUserProfile() ?: UserProfile()
        fitnessDao.insertUserProfile(p.copy(isPremium = isPremium, subscriptionType = subType))
    }

    // Exercises Libraries
    val allExercises: Flow<List<Exercise>> = fitnessDao.getAllExercisesFlow()

    fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return fitnessDao.getExercisesByCategoryFlow(category)
    }

    suspend fun insertExerciseItem(exercise: Exercise) {
        fitnessDao.insertExercise(exercise)
    }

    // Workout Plans
    val workoutPlan: Flow<List<WorkoutPlanItem>> = fitnessDao.getWorkoutPlanItemFlow()

    suspend fun saveWorkoutPlan(items: List<WorkoutPlanItem>) {
        fitnessDao.insertWorkoutPlanItems(items)
    }

    suspend fun updatePlanItemCompletion(item: WorkoutPlanItem, isCompleted: Boolean) {
        fitnessDao.updateWorkoutPlanItem(item.copy(completed = isCompleted))
        if (isCompleted) {
            addXpAndCheckStreak(20) // Award 20 XP for each exercise completed
        }
    }

    suspend fun clearCurrentPlan() {
        fitnessDao.clearWorkoutPlan()
    }

    /**
     * AI Workout Generator: Populates customized plans based on target and preferences.
     */
    suspend fun generateAndSaveAIWorkoutPlan(
        goal: String,
        level: String,
        preference: String
    ) {
        clearCurrentPlan()
        
        val isHome = preference.equals("Home", ignoreCase = true)
        val dayPrefix = "Day 1 - $goal ($level)"

        val generatedItems = when {
            goal.contains("Muscle", ignoreCase = true) || goal.contains("Gain", ignoreCase = true) || goal.contains("Strength", ignoreCase = true) -> {
                if (isHome) {
                    listOf(
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Incline Push-ups (on Couch/Bed)", sets = 3, reps = "12-15", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Towel Door Rows / Pull-ups", sets = 3, reps = "10", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Bodyweight Squats with slow negatives", sets = 4, reps = "15-20", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Diamond Push-ups (Triceps focus)", sets = 3, reps = "8-10", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Plank Row Hovering", sets = 3, reps = "45s", restTime = "45s")
                    )
                } else {
                    listOf(
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Flat Barbell Bench Press", sets = 4, reps = "8-10", restTime = "90s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Barbell Back Squat", sets = 4, reps = "6-8", restTime = "120s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Lat Pulldown (Back focus)", sets = 4, reps = "10-12", restTime = "90s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Seated Dumbbell Shoulder Press", sets = 3, reps = "10-12", restTime = "90s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Incline Dumbbell Curls (Biceps)", sets = 3, reps = "12", restTime = "60s")
                    )
                }
            }
            goal.contains("Loss", ignoreCase = true) || goal.contains("Burn", ignoreCase = true) || goal.contains("Fat", ignoreCase = true) -> {
                if (isHome) {
                    listOf(
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "High Knees Sprinting in Place", sets = 4, reps = "45s", restTime = "30s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Jumping Squats (Explosive)", sets = 3, reps = "15-20", restTime = "45s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Mountain Climbers", sets = 4, reps = "30s", restTime = "30s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Burpees", sets = 3, reps = "10-12", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Cobra Pose & Breathing stretch", sets = 2, reps = "60s", restTime = "30s")
                    )
                } else {
                    listOf(
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Kettlebell Swings (High Intensity)", sets = 4, reps = "20", restTime = "45s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Dumbbell Thrusters", sets = 3, reps = "12-15", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Rowing Machine Sprint", sets = 4, reps = "500m", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Incline DB Press", sets = 3, reps = "12-15", restTime = "60s"),
                        WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Hanging Leg Raises", sets = 3, reps = "15", restTime = "45s")
                    )
                }
            }
            else -> { // Default general active training / yoga / cardio
                listOf(
                    WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Cobra Pose (Bhujangasana)", sets = 3, reps = "5 Breaths", restTime = "30s"),
                    WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Kettlebell Swings", sets = 3, reps = "15", restTime = "45s"),
                    WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Flat Barbell Bench Press", sets = 3, reps = "10", restTime = "90s"),
                    WorkoutPlanItem(dayName = dayPrefix, exerciseName = "Hanging Leg Raises", sets = 3, reps = "12", restTime = "45s")
                )
            }
        }
        fitnessDao.insertWorkoutPlanItems(generatedItems)
        addXpAndCheckStreak(50) // AI generation bonus XP!
    }

    // Diet Logs
    fun getDietLogsToday(): Flow<List<DietLog>> {
        return fitnessDao.getDietLogsByDateFlow(getTodayDateString())
    }

    suspend fun addDietLog(itemName: String, calories: Int, protein: Float, carbs: Float, fat: Float, mealType: String) {
        val log = DietLog(
            dateString = getTodayDateString(),
            itemName = itemName,
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            mealType = mealType
        )
        fitnessDao.insertDietLog(log)
        addXpAndCheckStreak(10) // Log food award 10 XP
    }

    suspend fun removeDietLog(log: DietLog) {
        fitnessDao.deleteDietLog(log)
    }

    // Water Tracker
    fun getWaterLogToday(): Flow<WaterLog?> {
        return fitnessDao.getWaterLogByDateFlow(getTodayDateString())
    }

    suspend fun updateWaterLog(glasses: Int) {
        val todayLog = WaterLog(dateString = getTodayDateString(), glasses = glasses)
        fitnessDao.insertWaterLog(todayLog)
        addXpAndCheckStreak(5) // Log water award 5 XP
    }

    // Chat logs
    val chatLogs: Flow<List<ChatLog>> = fitnessDao.getChatLogsFlow()

    suspend fun addChatLog(sender: String, text: String) {
        fitnessDao.insertChatLog(ChatLog(sender = sender, messageText = text))
    }

    suspend fun clearChat() {
        fitnessDao.clearChatLogs()
    }

    // Progress History
    val progressHistory: Flow<List<ProgressHistory>> = fitnessDao.getProgressHistoryFlow()

    suspend fun addProgressLog(weight: Float, height: Float, bmi: Float, fatEstimate: Float) {
        val p = ProgressHistory(
            weight = weight,
            height = height,
            bmi = bmi,
            bodyFatEstimate = fatEstimate
        )
        fitnessDao.insertProgressHistory(p)
        
        // Also update the main profile weight
        val currentProfile = fitnessDao.getUserProfile() ?: UserProfile()
        fitnessDao.insertUserProfile(currentProfile.copy(weight = weight, height = height))
        
        addXpAndCheckStreak(30) // Award 30 XP for tracking progress
    }

    // Community Posts
    val communityPosts: Flow<List<CommunityPost>> = fitnessDao.getCommunityPostsFlow()

    suspend fun addCommunityPost(username: String, title: String, content: String) {
        val post = CommunityPost(
            username = username,
            userTitle = title,
            content = content,
            dateString = "Just now"
        )
        fitnessDao.insertCommunityPost(post)
        addXpAndCheckStreak(25) // Award XP for sharing in community
    }

    suspend fun likeCommunityPost(post: CommunityPost) {
        val isLiked = post.likedByMe
        val newLikesCount = if (isLiked) post.likes - 1 else post.likes + 1
        fitnessDao.updateCommunityPost(post.copy(likedByMe = !isLiked, likes = newLikesCount))
    }
}
