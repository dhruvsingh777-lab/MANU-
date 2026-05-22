package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FitnessRepository(application)

    // Collect variables from repository
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val workoutPlan: StateFlow<List<WorkoutPlanItem>> = repository.workoutPlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dietLogs: StateFlow<List<DietLog>> = repository.getDietLogsToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterLog: StateFlow<WaterLog?> = repository.getWaterLogToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatLogs: StateFlow<List<ChatLog>> = repository.chatLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressHistory: StateFlow<List<ProgressHistory>> = repository.progressHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    // Dashboard daily metrics derived states
    val caloriesBurnedSum = workoutPlan.map { list ->
        list.filter { it.completed }.size * 150 // Assume 150 cal burned per exercise completed
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val caloriesEatenSum = dietLogs.map { list ->
        list.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val proteinEatenSum = dietLogs.map { list ->
        list.sumOf { it.proteinGrams.toDouble() }.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Set User Profile (Onboarding Questionnaire)
    fun completeOnboarding(
        age: Int,
        weight: Float,
        height: Float,
        gender: String,
        goal: String,
        experience: String,
        preference: String
    ) {
        viewModelScope.launch {
            val updated = UserProfile(
                id = 1,
                age = age,
                weight = weight,
                height = height,
                gender = gender,
                fitnessGoal = goal,
                experienceLevel = experience,
                workoutPreference = preference,
                onboardingCompleted = true,
                xpPoints = 100, // starting xp points!
                dailyStreak = 1
            )
            repository.saveUserProfile(updated)
            // Immediately generate their custom AI plan based on these preferences!
            repository.generateAndSaveAIWorkoutPlan(goal, experience, preference)
        }
    }

    // Generate/Regenerate Plan
    fun regenerateAIWorkoutPlan() {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.generateAndSaveAIWorkoutPlan(
                profile.fitnessGoal,
                profile.experienceLevel,
                profile.workoutPreference
            )
        }
    }

    // Check action
    fun toggleWorkoutItem(item: WorkoutPlanItem, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updatePlanItemCompletion(item, isCompleted)
        }
    }

    // Log Nutrition food
    fun addMealItem(name: String, cal: Int, protein: Float, carbs: Float, fat: Float, mealType: String) {
        viewModelScope.launch {
            repository.addDietLog(name, cal, protein, carbs, fat, mealType)
        }
    }

    fun removeMealItem(log: DietLog) {
        viewModelScope.launch {
            repository.removeDietLog(log)
        }
    }

    // Log Water
    fun addWaterGlass() {
        viewModelScope.launch {
            val current = waterLog.value?.glasses ?: 0
            repository.updateWaterLog(current + 1)
        }
    }

    fun removeWaterGlass() {
        viewModelScope.launch {
            val current = waterLog.value?.glasses ?: 0
            if (current > 0) {
                repository.updateWaterLog(current - 1)
            }
        }
    }

    // AI Coach Interaction
    fun sendMessageToAI(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSendingChat.value = true
            // Save user's question first
            repository.addChatLog("USER", text)
            
            // Generate Coach Answer
            val response = GeminiNetwork.getCoachResponse(text, chatLogs.value)
            
            // Save coach response
            repository.addChatLog("COACH", response)
            _isSendingChat.value = false
            
            // Award chat XP points to keep them motivated!
            repository.addXpAndCheckStreak(15)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Progress Log
    fun logProgress(weight: Float) {
        viewModelScope.launch {
            val heightCm = userProfile.value.height
            val heightM = heightCm / 100f
            val bmi = if (heightM > 0f) weight / (heightM * heightM) else 0f
            
            // Approximate Body Fat estimation based on gender and BMI (Adult YMCA / Deurenberg formula)
            // Body fat % = (1.2 * BMI) + (0.23 * age) - (10.8 * genderFactor) - 5.4
            // genderFactor: Male = 1, Female = 0
            val age = userProfile.value.age
            val genderFactor = if (userProfile.value.gender.lowercase() == "male") 1 else 0
            val bodyFat = ((1.2f * bmi) + (0.23f * age) - (10.8f * genderFactor) - 5.4f).coerceIn(5f, 50f)
            
            // Format to 1 decimal place
            val roundedBmi = (bmi * 10).roundToInt() / 10f
            val roundedBodyFat = (bodyFat * 10).roundToInt() / 10f

            repository.addProgressLog(weight, heightCm, roundedBmi, roundedBodyFat)
        }
    }

    // Community Posting
    fun writeCommunityPost(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val title = when {
                profile.xpPoints > 1000 -> "Gym Town Legend 👑"
                profile.xpPoints > 500 -> "Heavy Lifter 🏋️"
                profile.xpPoints > 250 -> "Iron Enthusiast 🔥"
                else -> "Gym Beginner 🌱"
            }
            repository.addCommunityPost("User_${profile.fitnessGoal.replace(" ", "")}", title, content)
        }
    }

    fun toggleLikePost(post: CommunityPost) {
        viewModelScope.launch {
            repository.likeCommunityPost(post)
        }
    }

    // Purchase premium simulation
    fun buyPremium(planName: String) {
        viewModelScope.launch {
            repository.updateSubscription(true, "Premium ($planName)")
            repository.addXpAndCheckStreak(100) // Huge bonus XP for going premium!
        }
    }

    fun cancelPremium() {
        viewModelScope.launch {
            repository.updateSubscription(false, "Free")
        }
    }
}
