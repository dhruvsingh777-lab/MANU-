package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetwork {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Call the Gemini flash API to generate advice.
     */
    suspend fun getCoachResponse(prompt: String, contextHistory: List<ChatLog>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            return getFallbackFitnessAdvice(prompt)
        }

        val fullSystemPrompt = """
            You are "Coach Manu", the expert head AI fitness, bodybuilding, and nutrition coach at MANU GYM TOWN.
            Your target audience includes gym beginners, professional bodybuilders, home-workout enthusiasts, and weight-loss seekers.
            Always maintain a motivating, high-energy, and professional coaching tone.
            Include Indian meal advice and high-protein vegetarian/vegan diet suggestions when appropriate.
            Keep responses clear, concise, structured with bullet points, and actionable for fitness.
        """.trimIndent()

        // Combine system instruction + recent chat history + current prompt
        val contents = mutableListOf<GeminiContent>()
        
        // Add current prompt with system context
        val textWithSystem = "System Instruction: $fullSystemPrompt\n\nUser Question: $prompt"
        contents.add(GeminiContent(parts = listOf(GeminiPart(text = textWithSystem))))

        val request = GeminiRequest(contents = contents)

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I am here to guide you, but I couldn't generate a text response right now. Ask me anything about exercise or diet!"
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback to rich local logic
            getFallbackFitnessAdvice(prompt)
        }
    }

    /**
     * Elegant local model of coaching for offline or dev fallback.
     */
    private fun getFallbackFitnessAdvice(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("diet") || query.contains("meal") || query.contains("food") || query.contains("eat") -> {
                """
                    👋 *Coach Manu here!* (Offline fallback mode active)
                    
                    For a clean Indian fitness diet, prioritize these high-quality macros:
                    
                    • **Protein Sources**: Paneer (low fat), Soya chunks, Double-toned Milk, Curd, Moong Dal, boiled eggs (if non-veg), Sattu, and Whey Protein.
                    • **Carbohydrates**: Oats, Ragi, Brown Rice, Multi-grain Chapati.
                    • **Healthy Fats**: Almonds, Walnuts, Chia seeds, Peanut butter.
                    
                    **Sample Muscle-Gain Meal Plan:**
                    - **Breakfast**: Stuffed Paneer Chilla + 1 glass of Double-Toned Milk.
                    - **Lunch**: Brown Rice/Chapatis + Dal Tadka + Greek Curd Salad + Soya Chunks dry sabzi.
                    - **Evening Snack**: Roasted Chana + handful of almonds.
                    - **Dinner**: Tofu/Paneer Bhurji with Roti + clean greens.
                """.trimIndent()
            }
            query.contains("workout") || query.contains("gym") || query.contains("push") || query.contains("pull") || query.contains("leg") -> {
                """
                    👋 *Coach Manu here!* (Offline fallback mode active)
                    
                    Here is a solid **Push-Pull-Legs (PPL)** workout plan to build maximum muscle and strength:
                    
                    • **Push Day (Chest/Shoulders/Triceps)**:
                      - Incline Dumbbell Press: 3 Sets x 10 Reps (Rest: 90s)
                      - Overhead Dumbbell Press: 3 Sets x 10 Reps (Rest: 90s)
                      - Lateral Raises: 3 Sets x 15 Reps (Rest: 60s)
                      - Triceps Rope Pushdown: 3 Sets x 12 Reps (Rest: 60s)
                      
                    • **Pull Day (Back/Biceps)**:
                      - Lat Pulldowns: 4 Sets x 10 Reps (Rest: 90s)
                      - Dumbbell Rows: 3 Sets x 12 Reps (Rest: 90s)
                      - Hammer Curls: 3 Sets x 12 Reps (Rest: 60s)
                      
                    • **Leg Day (Quad/Hamstring/Calves/Core)**:
                      - Goblet Squat (or Barbell Squat): 4 Sets x 8-10 Reps (Rest: 120s)
                      - Romanian Deadlift: 3 Sets x 10 Reps (Rest: 90s)
                      - Cable Crunch (Abs): 3 Sets x 15 Reps (Rest: 60s)
                """.trimIndent()
            }
            query.contains("protein") || query.contains("calculate") || query.contains("calc") -> {
                """
                    👋 *Coach Manu here!* (Offline fallback mode active)
                    
                    To Calculate your daily protein intake:
                    
                    • **Sedentary lifestyle**: 0.8g to 1g per kg of bodyweight.
                    • **Active Fitness enthusiast**: 1.2g to 1.6g per kg of bodyweight.
                    • **Bodybuilding / Muscle building intense block**: 1.6g to 2.2g per kg of bodyweight.
                    
                    *Example: If you weigh 70kg, aim for **112g to 140g** of high-quality protein per day to repair and build lean muscle!*
                """.trimIndent()
            }
            query.contains("weight loss") || query.contains("fat burn") || query.contains("belly") -> {
                """
                    👋 *Coach Manu here!* (Offline fallback mode active)
                    
                    To burn fat and lose weight high-efficiently:
                    
                    1. **Caloric Deficit**: Eat about 300-500 calories below your TDEE maintenance level. Keep track in our Diet Planner!
                    2. **Keep Protein High**: 1.5g per kg to preserve muscle mass while burning body fat.
                    3. **Resistance Training**: Lift weights 3-4 days a week. Doing only cardio is a mistake.
                    4. **Daily Steps**: Aim for **8,000 to 10,000 steps** daily!
                """.trimIndent()
            }
            else -> {
                """
                    👋 *Hey! Coach Manu here at MANU GYM TOWN!*
                    
                    I am your personal AI Fitness & Bodybuilding Coach. You can ask me any specific question about:
                    
                    - 💪 **Custom workout splits** (e.g. Home workouts, Dumbbell only, Gym PPL)
                    - 🥗 **Indian Diet and macro calculations** (Veg, Vegan, High-protein)
                    - 📈 **Body weight progression, recovery, and stretching tips**
                    
                    Tell me: What is your current height, weight, and fitness goal? Let's crush this transformation journey together!
                """.trimIndent()
            }
        }
    }
}
