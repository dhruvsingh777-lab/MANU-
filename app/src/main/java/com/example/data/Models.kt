package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val age: Int = 24,
    val weight: Float = 70.0f,
    val height: Float = 175.0f,
    val gender: String = "Male",
    val fitnessGoal: String = "Muscle Gain",
    val experienceLevel: String = "Beginner",
    val workoutPreference: String = "Gym",
    val xpPoints: Int = 150,
    val dailyStreak: Int = 3,
    val lastStreakUpdate: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false,
    val subscriptionType: String = "Free",
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Chest", "Back", "Shoulders", "Legs", "Arms", "Abs", "Cardio", "Yoga"
    val targetMuscles: String,
    val caloriesBurned: Int,
    val instructions: String,
    val commonMistakes: String,
    val durationSeconds: Int = 0 // for timer-based exercise (yoga/cardio)
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayName: String, // "Day 1 - Push", "Day 2 - Pull", etc.
    val exerciseName: String,
    val sets: Int,
    val reps: String,
    val restTime: String,
    val feedback: String = "",
    val completed: Boolean = false
)

@Entity(tableName = "diet_logs")
data class DietLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // "YYYY-MM-DD"
    val itemName: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val mealType: String // "Breakfast", "Lunch", "Dinner", "Snack"
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val dateString: String, // "YYYY-MM-DD"
    val glasses: Int
)

@Entity(tableName = "chat_logs")
data class ChatLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "COACH"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_history")
data class ProgressHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val weight: Float,
    val height: Float = 175f,
    val bmi: Float,
    val bodyFatEstimate: Float
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userTitle: String, // e.g. "Gym Legend", "Pumping Iron"
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val dateString: String = "Today",
    val commentsCount: Int = 0
)
