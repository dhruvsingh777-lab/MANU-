package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FitnessViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val fitnessViewModel: FitnessViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController, fitnessViewModel)
                    }
                    composable("login") {
                        LoginScreen(navController, fitnessViewModel)
                    }
                    composable("onboarding") {
                        OnboardingScreen(navController, fitnessViewModel)
                    }
                    composable("dashboard") {
                        DashboardScreen(navController, fitnessViewModel)
                    }
                    composable("workout") {
                        WorkoutScreen(navController, fitnessViewModel)
                    }
                    composable("exercise_detail/{exerciseId}") { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                        ExerciseDetailScreen(navController, fitnessViewModel, exerciseId)
                    }
                    composable("diet") {
                        DietPlannerScreen(navController, fitnessViewModel)
                    }
                    composable("chat") {
                        ChatCoachScreen(navController, fitnessViewModel)
                    }
                    composable("progress") {
                        ProgressAnalyticsScreen(navController, fitnessViewModel)
                    }
                    composable("community") {
                        CommunityScreen(navController, fitnessViewModel)
                    }
                    composable("subscription") {
                        SubscriptionScreen(navController, fitnessViewModel)
                    }
                    composable("profile") {
                        ProfileScreen(navController, fitnessViewModel)
                    }
                }
            }
        }
    }
}
