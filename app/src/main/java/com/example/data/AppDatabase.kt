package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        Exercise::class,
        WorkoutPlanItem::class,
        DietLog::class,
        WaterLog::class,
        ChatLog::class,
        ProgressHistory::class,
        CommunityPost::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymtown_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            instance?.let { database ->
                                seedDatabase(database.fitnessDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                
                instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private fun updateInstance(instance: AppDatabase) {
            INSTANCE = instance
        }

        private suspend fun seedDatabase(dao: FitnessDao) {
            // Seed User Profile
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    onboardingCompleted = false,
                    xpPoints = 120,
                    dailyStreak = 4,
                    fitnessGoal = "Muscle Gain"
                )
            )

            // Seed Exercises Library
            val chestExercises = listOf(
                Exercise(
                    name = "Incline Dumbbell Press",
                    category = "Chest",
                    targetMuscles = "Upper Pectorals, Anterior Deltoids, Triceps",
                    caloriesBurned = 120,
                    instructions = "1. Set bench to 30-45 degree incline.\n2. Keep feet flat on the floor, bring dumbbells to chest height.\n3. Contract your chest and push dumbbells up, keeping elbows tucked slightly.\n4. Lower down with control to starting position.",
                    commonMistakes = "Incorrect bench angle, flaring elbows out too wide, arching lower back Excessively."
                ),
                Exercise(
                    name = "Flat Barbell Bench Press",
                    category = "Chest",
                    targetMuscles = "Mid-Lower Pectorals, Anterior Deltoids, Triceps",
                    caloriesBurned = 150,
                    instructions = "1. Lie flat on bench. Grip bar wider than shoulder-width.\n2. Unrack bar, lower with control to lower chest.\n3. Drive feet into floor and press bar up vertically, contracting the chest.\n4. Lock out elbows at the top.",
                    commonMistakes = "Bouncing bar off the chest, lifting shoulders off the bench, flaring elbows."
                )
            )

            val backExercises = listOf(
                Exercise(
                    name = "Lat Pulldown",
                    category = "Back",
                    targetMuscles = "Latissimus Dorsi, Rhomboids, Teres Major, Biceps",
                    caloriesBurned = 110,
                    instructions = "1. Sit facing stack, knee pads secure. Grab bar wide.\n2. Pull shoulder blades down, lean back slightly.\n3. Drive elbows down to side, pulling bar to upper chest.\n4. Squeeze shoulder blades, slowly extend arms back to start.",
                    commonMistakes = "Pulling with your forearms/hands, swinging your head/torso violently, pulling bar too low."
                ),
                Exercise(
                    name = "Barbell Row",
                    category = "Back",
                    targetMuscles = "Rhomboids, Lats, Trapezius, Rear Delts",
                    caloriesBurned = 140,
                    instructions = "1. Stand with feet shoulder-width, grab bar with overhand grip.\n2. Hinge at hips with back straight and flat until torso is parallel to floor.\n3. Pull the bar to your lower ribs, driving elbows up.\n4. Squeeze back, then lower with control.",
                    commonMistakes = "Rounding lower back, lifting torso up to cheat, pulling too fast."
                )
            )

            val shoulderExercises = listOf(
                Exercise(
                    name = "Seated Overhead Dumbbell Press",
                    category = "Shoulders",
                    targetMuscles = "Anterior and Lateral Deltoids, Triceps",
                    caloriesBurned = 100,
                    instructions = "1. Sit on a vertical backed bench. Bring dumbbells to shoulders.\n2. Press dumbbells vertically, contracting shoulders until elbows lock.\n3. Lower dumbbells under control to ear height.\n4. Repeat.",
                    commonMistakes = "Arching lower back, letting dumbbells touch at the top, pressing forward instead of upward."
                ),
                Exercise(
                    name = "Lateral Raises",
                    category = "Shoulders",
                    targetMuscles = "Lateral Deltoids (Side Delts)",
                    caloriesBurned = 80,
                    instructions = "1. Stand tall, dumbbells at sides.\n2. Raise arms out sideways, leading with elbows, with a slight bend.\n3. Stop at shoulder level, pinkies slightly higher than thumbs.\n4. Return slowly to start.",
                    commonMistakes = "Using heavy weight and swinging body, raising hands higher than elbows, bending arms too much."
                )
            )

            val legExercises = listOf(
                Exercise(
                    name = "Barbell Back Squat",
                    category = "Legs",
                    targetMuscles = "Quadriceps, Glutes, Hamstrings, Spinal Erectors",
                    caloriesBurned = 180,
                    instructions = "1. Place bar on upper back. Feet shoulder-width, toes turned out.\n2. Inhale, brace core, bend knees, and push hips back to lower down.\n3. Descend until thighs are parallel to the floor or lower.\n4. Drive through heels, stand up straight, exhaling.",
                    commonMistakes = "Knees caving inwards, heels rising off the floor, rounding lower back."
                ),
                Exercise(
                    name = "Romanian Deadlift",
                    category = "Legs",
                    targetMuscles = "Hamstrings, Gluteus Maximus, Lower Back",
                    caloriesBurned = 160,
                    instructions = "1. Stand upright holding barbell at thighs, feet hip-width.\n2. Keep back flat, push hips backward to hinge forward.\n3. Lower bar down legs, feeling a deep stretch in hamstrings.\n4. Contract glutes and push hips forward to return.",
                    commonMistakes = "Squatting instead of hinging, rounding the spine, letting the bar drift away from legs."
                )
            )

            val armsExercises = listOf(
                Exercise(
                    name = "Incline Dumbbell Curl",
                    category = "Arms",
                    targetMuscles = "Biceps Brachii (Long Head), Brachialis",
                    caloriesBurned = 90,
                    instructions = "1. Sit on a 45-degree incline bench with dumbbells hanging straight.\n2. Pin elbows to sides, curl weights up keeping wrists neutral.\n3. Squeeze biceps at the top.\n4. Lower dumbbells fully to felt stretch under control.",
                    commonMistakes = "Moving elbows forward to chest height, arching back, swinging upper arms."
                ),
                Exercise(
                    name = "Triceps Rope Pushdowns",
                    category = "Arms",
                    targetMuscles = "Triceps Brachii (All Heads)",
                    caloriesBurned = 85,
                    instructions = "1. Attach rope to high cable. Hold rope, flex elbows.\n2. Pin elbows at sides, stand slightly leaning forward.\n3. Push cable down, splitting rope ends at bottom to fully lock out.\n4. Return under control.",
                    commonMistakes = "Allowing elbows to flare or drift forward, using weight too heavy raising shoulders."
                )
            )

            val absExercises = listOf(
                Exercise(
                    name = "Hanging Leg Raises",
                    category = "Abs",
                    targetMuscles = "Rectus Abdominis (Lower Core), Iliopsoas",
                    caloriesBurned = 95,
                    instructions = "1. Hang from a pullup bar, legs fully extended.\n2. Keep legs straight or slightly bent, raise legs using hip flexors and core.\n3. Stop when legs are parallel to the floor, squeeze abs.\n4. Lower legs fully with minimal swing.",
                    commonMistakes = "Swinging torso to gain momentum, pulling with shoulders, dropping legs too fast."
                ),
                Exercise(
                    name = "Cable Crunches",
                    category = "Abs",
                    targetMuscles = "Upper Rectus Abdominis, Obliques",
                    caloriesBurned = 80,
                    instructions = "1. Kneel facing stack under high cable rope pulley. Hold rope by ears.\n2. Exhale, round back and pull elbows towards thighs using abs.\n3. Contract core intensely, slowly return back to stretch.",
                    commonMistakes = "Pulling rope with arms instead of abs, moving hips/glutes back, keeping back flat."
                )
            )

            val cardioExercises = listOf(
                Exercise(
                    name = "Kettlebell Swing",
                    category = "Cardio",
                    targetMuscles = "Posterior Chain (Glutes, Hamstrings, Core, Back)",
                    caloriesBurned = 250,
                    instructions = "1. Hold kettlebell between legs, hips hinged back.\n2. Drive hips forward explosively, snapping glutes.\n3. Let kettlebell swing to shoulder height, keep arms relaxed.\n4. Hinge as bell falls down.",
                    commonMistakes = "Squatting instead of hinging, lifting kettlebell with front delts, rounding lower back."
                )
            )

            val yogaExercises = listOf(
                Exercise(
                    name = "Cobra Pose (Bhujangasana)",
                    category = "Yoga",
                    targetMuscles = "Spinal Erectors, Shoulders, Chest",
                    caloriesBurned = 50,
                    instructions = "1. Lie flat on stomach, palms by ribs.\n2. Keep chest lifted, elbows hugged to body.\n3. Press tops of feet down, lift torso using spinal muscles.\n4. Hold for 5 long breaths.",
                    commonMistakes = "Jamming neck back, placing too much weight on hands, lifting hips up."
                )
            )

            val allList = chestExercises + backExercises + shoulderExercises + legExercises + armsExercises + absExercises + cardioExercises + yogaExercises
            dao.insertExercises(allList)

            // Seed Workout Plan Day 1 as starter
            val starterPlan = listOf(
                WorkoutPlanItem(dayName = "Day 1 - Push Day", exerciseName = "Flat Barbell Bench Press", sets = 4, reps = "8-10", restTime = "90s"),
                WorkoutPlanItem(dayName = "Day 1 - Push Day", exerciseName = "Seated Overhead Dumbbell Press", sets = 3, reps = "10-12", restTime = "90s"),
                WorkoutPlanItem(dayName = "Day 1 - Push Day", exerciseName = "Triceps Rope Pushdowns", sets = 3, reps = "12-15", restTime = "60s")
            )
            dao.insertWorkoutPlanItems(starterPlan)

            // Seed Community Posts
            val seedPosts = listOf(
                CommunityPost(
                    username = "Manu_GymTown",
                    userTitle = "Head Coach & Founder",
                    content = "Welcome to MANU GYM TOWN! 💪 Fuel your body, train hard, and remember: Consistency beats motivation every single day. Let's make that transformation happen of age-defying fitness! What goal did you set in onboarding?",
                    likes = 45,
                    likedByMe = true,
                    dateString = "Today"
                ),
                CommunityPost(
                    username = "Aniket_Ripped",
                    userTitle = "Advanced Bodybuilder",
                    content = "Just hit a new PR at the gym - 140kg squat for 6 solid reps with full depth squatting! Push day later tonight. Indian high-protein vegetarian diet is doing wonders!",
                    likes = 12,
                    likedByMe = false,
                    dateString = "Yesterday"
                ),
                CommunityPost(
                    username = "Priya_FitIndian",
                    userTitle = "Yoga Coach",
                    content = "Starting the morning with daily Surya Namaskar and cobra postures! Extremely essential for spinal recovery after a heavy leg workout. Don't skip stretching, fam!",
                    likes = 28,
                    likedByMe = false,
                    dateString = "2 days ago"
                )
            )
            for (p in seedPosts) {
                dao.insertCommunityPost(p)
            }
        }
    }
}
