package com.example.ui.screens

import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.FitnessViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ==========================================
// CENTRAL DESIGN SYSTEM COMPONENTS (GLASSMORPHISM)
// ==========================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GymRed.copy(alpha = 0.2f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GymDarkGray.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassCardClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = GymRed.copy(alpha = 0.2f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GymDarkGray.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StatProgressRing(
    progress: Float, // 0f to 1f
    targetText: String,
    currentText: String,
    label: String,
    color: Color = GymRed,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background track
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Draw colored progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GymWhite
                )
            )
            Text(
                text = "/$targetText",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = GymGray
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// Custom Premium Bezier Graph for weight progress
@Composable
fun CustomWeightGraph(
    history: List<ProgressHistory>,
    modifier: Modifier = Modifier,
    lineColor: Color = GymRed
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.background(GymMediumGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No progress logs found. Add weight logs to view chart!",
                color = GymGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val displayList = if (history.size > 7) history.takeLast(7) else history

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val weights = displayList.map { it.weight }
        val minW = weights.minOrNull() ?: 0f
        val maxW = weights.maxOrNull() ?: 100f
        val range = if (maxW == minW) 10f else (maxW - minW) * 1.2f
        val baseMin = minW - (range * 0.1f)

        val points = displayList.mapIndexed { idx, prog ->
            val x = if (displayList.size > 1) {
                idx * (width / (displayList.size - 1))
            } else {
                width / 2
            }
            val y = height - ((prog.weight - baseMin) / range) * height
            Offset(x, y)
        }

        // Draw background grid lines back
        for (i in 1..3) {
            val yOffset = i * (height / 4)
            drawLine(
                color = GymWhite.copy(alpha = 0.05f),
                start = Offset(0f, yOffset),
                end = Offset(width, yOffset),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.size > 1) {
            // Draw gradient area under the curve
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                lineTo(points.first().x, points.first().y)
                
                // Draw curve using cubic line
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val conX1 = (p0.x + p1.x) / 2f
                    cubicTo(conX1, p0.y, conX1, p1.y, p1.x, p1.y)
                }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val conX1 = (p0.x + p1.x) / 2f
                    cubicTo(conX1, p0.y, conX1, p1.y, p1.x, p1.y)
                }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw points nodes
        points.forEachIndexed { idx, pt ->
            drawCircle(
                color = GymWhite,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = pt
            )
        }
    }
}


// ==========================================
// 1. SPLASH SCREEN
// ==========================================

@Composable
fun SplashScreen(navController: NavController, viewModel: FitnessViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(2000) // 2 sec delay
        if (profile.onboardingCompleted) {
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stylized giant dumbbell icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(GymRed.copy(alpha = 0.1f), CircleShape)
                    .border(2.dp, GymRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "MANU GYM TOWN Logo",
                    tint = GymRed,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "MANU GYM TOWN",
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                fontSize = 28.sp,
                color = GymWhite,
                letterSpacing = 4.sp
            )
            
            Text(
                text = "AI-POWERED BODYBUILDING & COACHING",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = GymRed,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = GymRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


// ==========================================
// 2. LOGIN / REGISTER SCREEN
// ==========================================

@Composable
fun LoginScreen(navController: NavController, viewModel: FitnessViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterState by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = GymRed,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "MANU GYM TOWN",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = GymWhite,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = if (isRegisterState) "Create your athletic profile" else "Welcome Back, Athlete!",
                color = GymGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            if (!otpSent) {
                // Email input
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = GymGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                        .padding(bottom = 12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = GymWhite,
                        unfocusedTextColor = GymWhite,
                        focusedContainerColor = GymDarkGray,
                        unfocusedContainerColor = GymDarkGray,
                        focusedIndicatorColor = GymRed,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Password Input
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = GymGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                        .padding(bottom = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = GymWhite,
                        unfocusedTextColor = GymWhite,
                        focusedContainerColor = GymDarkGray,
                        unfocusedContainerColor = GymDarkGray,
                        focusedIndicatorColor = GymRed,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (!isRegisterState) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { showForgotDialog = true }) {
                            Text("Forgot Password?", color = GymRed, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core submit trigger
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isRegisterState) {
                            otpSent = true
                            Toast.makeText(context, "OTP Code sent to $email", Toast.LENGTH_SHORT).show()
                        } else {
                            // Instant simulation login
                            Toast.makeText(context, "Logged in as $email", Toast.LENGTH_SHORT).show()
                            navController.navigate("onboarding")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRegisterState) "CONTINUE TO REGISTRATION" else "SIGN IN",
                        fontWeight = FontWeight.Bold,
                        color = GymWhite
                    )
                }
            } else {
                // OTP input configuration panel
                Text(
                    text = "Verify OTP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GymWhite,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "A 6-digit verification code has been sent. Enter 123456 to bypass simulation.",
                    color = GymGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    label = { Text("6-Digit OTP", color = GymGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = GymWhite,
                        focusedContainerColor = GymDarkGray,
                        unfocusedContainerColor = GymDarkGray,
                        focusedIndicatorColor = GymRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Button(
                    onClick = {
                        if (otpCode == "123456" || otpCode.length >= 4) {
                            Toast.makeText(context, "OTP Verified successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate("onboarding")
                        } else {
                            Toast.makeText(context, "Incorrect OTP. Code is 123456", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VERIFY & REGISTER", fontWeight = FontWeight.Bold, color = GymWhite)
                }

                TextButton(onClick = { otpSent = false }, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Back to Email edit", color = GymGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider row
            if (!otpSent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GymWhite.copy(alpha = 0.1f))
                    Text("OR CONTINUE WITH", color = GymGray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GymWhite.copy(alpha = 0.1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Google Login Button
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Google Sign-In Approved", Toast.LENGTH_SHORT).show()
                        navController.navigate("onboarding")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, GymWhite.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GymWhite)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Google Logo",
                            tint = GymRed,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Google Sign-In", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Toggle register mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isRegisterState) "Already have an account?" else "New to GYM TOWN?",
                        color = GymGray,
                        fontSize = 13.sp
                    )
                    TextButton(onClick = { isRegisterState = !isRegisterState }) {
                        Text(
                            text = if (isRegisterState) "Sign In" else "Register Now",
                            color = GymRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Forgot password simulated dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password Link", color = GymWhite) },
            text = {
                Column {
                    Text("Enter your email address and we will mail you an instruction link.", color = GymGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    TextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("E-mail") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = GymWhite,
                            focusedContainerColor = GymMediumGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotDialog = false
                        Toast.makeText(context, "Instruction link sent to $forgotEmail", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                ) {
                    Text("SEND Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) { Text("Cancel", color = GymWhite) }
            },
            containerColor = GymDarkGray
        )
    }
}


// ==========================================
// 3. ONBOARDING SCREEN
// ==========================================

@Composable
fun OnboardingScreen(navController: NavController, viewModel: FitnessViewModel) {
    var step by remember { mutableStateOf(1) }

    // Questionnaire Answers
    var age by remember { mutableStateOf(24) }
    var weight by remember { mutableStateOf(70f) }
    var height by remember { mutableStateOf(175f) }
    var gender by remember { mutableStateOf("Male") }
    var goal by remember { mutableStateOf("Muscle Gain") }
    var level by remember { mutableStateOf("Beginner") }
    var preference by remember { mutableStateOf("Gym") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBlack)
            .padding(24.dp)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with progression indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATHLETE ONBOARDING",
                    fontWeight = FontWeight.Black,
                    color = GymRed,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$step/4",
                    color = GymWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            LinearProgressIndicator(
                progress = { step / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp)
                    .clip(CircleShape),
                color = GymRed,
                trackColor = GymDarkGray
            )

            // Dynamic Step Form Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (step) {
                    1 -> {
                        Column {
                            Text("TELL US CORES ABOUT YOU", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GymWhite)
                            Text("This calculates your body metrics and targets calorie limits.", color = GymGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 28.dp))

                            // Gender dropdown/selectors
                            Text("GENDER", color = GymRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)) {
                                val genders = listOf("Male", "Female", "Other")
                                genders.forEach { g ->
                                    val isSel = gender == g
                                    Button(
                                        onClick = { gender = g },
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) GymRed else GymDarkGray
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(g, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Age Selector slider
                            Text("AGE: $age Years", color = GymWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                            Slider(
                                value = age.toFloat(),
                                onValueChange = { age = it.roundToInt() },
                                valueRange = 16f..70f,
                                colors = SliderDefaults.colors(thumbColor = GymRed, activeTrackColor = GymRed)
                            )

                            // Height Selector slider
                            Text("HEIGHT: ${height.roundToInt()} cm", color = GymWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
                            Slider(
                                value = height,
                                onValueChange = { height = it },
                                valueRange = 120f..220f,
                                colors = SliderDefaults.colors(thumbColor = GymRed, activeTrackColor = GymRed)
                            )
                        }
                    }
                    2 -> {
                        Column {
                            Text("CURRENT BODY WEIGHT", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GymWhite)
                            Text("We will configure your muscle progression logs.", color = GymGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 28.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${String.format("%.1f", weight)} kg",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GymWhite
                                    )
                                    Text(
                                        text = "${(weight * 2.20462f).roundToInt()} lbs",
                                        color = GymRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Slider(
                                value = weight,
                                onValueChange = { weight = it },
                                valueRange = 40f..150f,
                                colors = SliderDefaults.colors(thumbColor = GymRed, activeTrackColor = GymRed)
                            )
                        }
                    }
                    3 -> {
                        Column {
                            Text("CHOOSE MAIN FITNESS GOAL", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GymWhite)
                            Text("We will tailor diet plans & coach advice.", color = GymGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp))

                            val goals = listOf(
                                "Muscle Gain" to "Gain lean mass and overall strength size.",
                                "Weight Loss" to "Burn stubborn visceral fat and increase definition.",
                                "Fat Burn" to "Active cutting phase, preserving muscles.",
                                "Strength Block" to "Increase compound lift numbers."
                            )

                            goals.forEach { item ->
                                val isSel = goal == item.first
                                GlassCardClickable(
                                    onClick = { goal = item.first },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    borderColor = if (isSel) GymRed else GymWhite.copy(alpha = 0.05f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSel,
                                            onClick = { goal = item.first },
                                            colors = RadioButtonDefaults.colors(selectedColor = GymRed)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(item.first, fontWeight = FontWeight.Bold, color = GymWhite)
                                            Text(item.second, color = GymGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        Column {
                            Text("EXPERIENCE & PREFERENCES", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GymWhite)
                            Text("Tell us how you train.", color = GymGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 20.dp))

                            // Experience Level
                            Text("EXPERIENCE LEVEL", color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            val levels = listOf("Beginner", "Intermediate", "Advanced")
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)) {
                                levels.forEach { l ->
                                    val isSel = level == l
                                    Button(
                                        onClick = { level = l },
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) GymRed else GymDarkGray
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(l, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Workout preference
                            Text("TRAINING ENVIRONMENT", color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            val envs = listOf("Gym" to "Full access to barbell and weights", "Home" to "Bodyweight movements & resistance bands")
                            envs.forEach { pair ->
                                val isSel = preference == pair.first
                                GlassCardClickable(
                                    onClick = { preference = pair.first },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    borderColor = if (isSel) GymRed else GymWhite.copy(alpha = 0.05f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (pair.first == "Gym") Icons.Default.FitnessCenter else Icons.Default.Home,
                                            contentDescription = null,
                                            tint = if (isSel) GymRed else GymGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.padding(start = 12.dp)) {
                                            Text(pair.first, fontWeight = FontWeight.Bold, color = GymWhite)
                                            Text(pair.second, color = GymGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Navigation Row bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        border = BorderStroke(1.dp, GymWhite.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymWhite)
                    ) {
                        Text("PREV")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            // Finish and submit profile onboarding!
                            viewModel.completeOnboarding(
                                age = age,
                                weight = weight,
                                height = height,
                                gender = gender,
                                goal = goal,
                                experience = level,
                                preference = preference
                            )
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (step == 4) "FINISH & GENERATE AI PLAN" else "NEXT",
                            fontWeight = FontWeight.Bold,
                            color = GymWhite
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = GymWhite,
                            modifier = Modifier.padding(start = 4.dp).size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// CENTRAL NAVIGATION BOTTOM CONTAINER
// ==========================================

@Composable
fun AppBottomLayout(
    navController: NavController,
    currentRoute: String,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = GymDarkGray,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("dashboard", "Home", Icons.Default.Dashboard),
                    Triple("workout", "Workout", Icons.Default.FitnessCenter),
                    Triple("diet", "Diet", Icons.Default.RestaurantMenu),
                    Triple("chat", "AI Coach", Icons.Default.ChatBubble),
                    Triple("community", "Town Feed", Icons.Default.People)
                )

                items.forEach { (route, label, icon) ->
                    val isSel = currentRoute == route
                    NavigationBarItem(
                        selected = isSel,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = icon, contentDescription = label)
                        },
                        label = {
                            Text(label, fontSize = 10.sp, maxLines = 1)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymRed,
                            selectedTextColor = GymRed,
                            unselectedIconColor = GymGray,
                            unselectedTextColor = GymGray,
                            indicatorColor = GymRed.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        },
        content = content
    )
}


// ==========================================
// 4. HOME DASHBOARD SCREEN
// ==========================================

@Composable
fun DashboardScreen(navController: NavController, viewModel: FitnessViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val plan by viewModel.workoutPlan.collectAsState()
    val dietLogs by viewModel.dietLogs.collectAsState()
    val waterLog by viewModel.waterLog.collectAsState()

    val caloriesBurned by viewModel.caloriesBurnedSum.collectAsState()
    val caloriesEaten by viewModel.caloriesEatenSum.collectAsState()
    val proteinGrams by viewModel.proteinEatenSum.collectAsState()

    val context = LocalContext.current
    val completedExercises = plan.filter { it.completed }.size

    AppBottomLayout(navController = navController, currentRoute = "dashboard") { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Streak Welcome Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(GymRed, CircleShape)
                                .clickable { navController.navigate("profile") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profile.gender.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = GymWhite
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "HELLO, CHAMP!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = GymWhite
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "XP Icon",
                                    tint = GymRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Level ${profile.xpPoints / 100} • ${profile.xpPoints} XP",
                                    fontSize = 11.sp,
                                    color = GymGray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // Streak Badge
                    Row(
                        modifier = Modifier
                            .background(GymRed.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .border(1.dp, GymRed.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Fire",
                            tint = GymRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${profile.dailyStreak} DAYS STREAK",
                            color = GymWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Highlight Motivation Quote Box
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = GymRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (profile.fitnessGoal == "Muscle Gain") {
                                    "\"The steel in the gym never lies. 100kg is always 100kg. Let's build!\""
                                } else {
                                    "\"Slow progress is better than no progress. Burn that fat, reveal the athlete!\""
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = GymWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "— Coach Manu",
                                fontSize = 10.sp,
                                color = GymRed,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            // Stat rings metrics row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val burnGoal = 400
                    val calorieGoal = 2000
                    val proteinGoal = (profile.weight * 2).roundToInt() // 2g per kg

                    StatProgressRing(
                        progress = if (calorieGoal > 0) caloriesEaten.toFloat() / calorieGoal else 0f,
                        targetText = "$calorieGoal kcal",
                        currentText = "$caloriesEaten",
                        label = "DIET INTAKE"
                    )

                    StatProgressRing(
                        progress = if (burnGoal > 0) caloriesBurned.toFloat() / burnGoal else 0f,
                        targetText = "$burnGoal kcal",
                        currentText = "$caloriesBurned",
                        label = "BURNED OUT",
                        color = Color.Green
                    )

                    StatProgressRing(
                        progress = if (proteinGoal > 0) proteinGrams / proteinGoal else 0f,
                        targetText = "${proteinGoal}g",
                        currentText = "${proteinGrams.roundToInt()}g",
                        label = "PROTEIN",
                        color = GymWhite
                    )
                }
            }

            // Bottom stats row of steps counter, water count
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Simulated Steps count
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = GymRed)
                        Text("STEP COUNTER", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Text("6,245", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        Text("Goal is 8,000 steps", fontSize = 9.sp, color = GymGray)
                    }

                    // Water intake counting card
                    val waterGlasses = waterLog?.glasses ?: 0
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = GymRed)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(GymRed.copy(alpha = 0.2f), CircleShape)
                                        .clickable { viewModel.removeWaterGlass() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = GymRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(GymRed, CircleShape)
                                        .clickable { viewModel.addWaterGlass() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = GymWhite, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text("WATER INTAKE", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Text("$waterGlasses Glasses", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        Text("Target: 10 glasses", fontSize = 9.sp, color = GymGray)
                    }
                }
            }

            // Quick AI prompts shortcuts triggers
            item {
                Text(
                    text = "AI FITNESS RECOMMENDATIONS & SHORTCUTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GymRed
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val queries = listOf(
                        "How do I gain lower chest line?" to Icons.Default.FitnessCenter,
                        "Paneer macro breakdown" to Icons.Default.RestaurantMenu,
                        "Explain correct squat depth" to Icons.Default.Info,
                        "What is a standard fat cutting deficit?" to Icons.Default.LocalFireDepartment
                    )

                    items(queries) { (q, ic) ->
                        GlassCardClickable(
                            onClick = {
                                viewModel.sendMessageToAI(q)
                                navController.navigate("chat") { launchSingleTop = true }
                            },
                            borderColor = GymWhite.copy(alpha = 0.05f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = ic, contentDescription = null, tint = GymRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = q,
                                    color = GymWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Workout Status Brief Card
            item {
                GlassCardClickable(
                    onClick = { navController.navigate("workout") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val curDay = plan.firstOrNull()?.dayName ?: "No dynamic active plan"
                            Text("ACTIVE WORKOUT PLAN", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                            Text(curDay, fontSize = 16.sp, fontWeight = FontWeight.Black, color = GymWhite)
                            Text("Progress: $completedExercises/${plan.size} Exercises Checked", fontSize = 12.sp, color = GymGray)
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GymRed)
                    }
                }
            }

            // Progress Quick Analytics Shortcut Box
            item {
                Text(
                    text = "PROGRESS HISTORY ROW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = GymRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                val weightHistory by viewModel.progressHistory.collectAsState()
                GlassCardClickable(
                    onClick = { navController.navigate("progress") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("BODY PROGRESS SNAPSHOT", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                                Text("Weight Curve Tracker", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GymWhite)
                            }
                            Text(text = "View Analytics", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomWeightGraph(
                            history = weightHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ==========================================
// 5. WORKOUT ROUTINE SCREEN
// ==========================================

@Composable
fun WorkoutScreen(navController: NavController, viewModel: FitnessViewModel) {
    val plan by viewModel.workoutPlan.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()

    val context = LocalContext.current

    AppBottomLayout(navController = navController, currentRoute = "workout") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANU GYM TOWN PLANS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GymWhite
                        )
                    )
                    Text(
                        text = "Tailored for ${profile.fitnessGoal} (${profile.experienceLevel})",
                        color = GymGray,
                        fontSize = 11.sp
                    )
                }

                // Restart generator button
                Button(
                    onClick = {
                        viewModel.regenerateAIWorkoutPlan()
                        Toast.makeText(context, "AI Regenerated new routines plan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkGray),
                    border = BorderStroke(1.dp, GymRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = GymRed, modifier = Modifier.size(16.dp))
                    Text("Regen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymWhite, modifier = Modifier.padding(start = 4.dp))
                }
            }

            Text("DAILY EXERCISE ACTIVITIES", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)

            // Routine checklist
            if (plan.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active workout generated yet.", color = GymGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.regenerateAIWorkoutPlan() },
                            colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                        ) {
                            Text("Generate Plan Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(plan) { item ->
                        val matchedExe = exercises.find { it.name.lowercase() == item.exerciseName.lowercase() }
                        FitnessWorkoutItemCard(
                            item = item,
                            hasGuide = matchedExe != null,
                            onCompleteToggle = { isChecked ->
                                viewModel.toggleWorkoutItem(item, isChecked)
                            },
                            onGuideClick = {
                                if (matchedExe != null) {
                                    navController.navigate("exercise_detail/${matchedExe.name}")
                                } else {
                                    // Make new placeholder one
                                    Toast.makeText(context, "No guide database found for ${item.exerciseName}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    // Exercises category selector library
                    item {
                        Text("EXPLORE EXERCISES LIBRARY", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        
                        val categories = listOf("Chest", "Back", "Shoulders", "Legs", "Arms", "Abs", "Cardio", "Yoga")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                Button(
                                    onClick = {
                                        navController.navigate("exercise_detail/category_$cat")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkGray),
                                    border = BorderStroke(1.dp, GymRed.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymWhite)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FitnessWorkoutItemCard(
    item: WorkoutPlanItem,
    hasGuide: Boolean,
    onCompleteToggle: (Boolean) -> Unit,
    onGuideClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (item.completed) Color.Green.copy(alpha = 0.3f) else GymRed.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = onCompleteToggle,
                colors = CheckboxDefaults.colors(checkedColor = Color.Green, checkmarkColor = GymBlack)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.exerciseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (item.completed) GymGray else GymWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.sets} Sets x ${item.reps} Reps • Rest: ${item.restTime}",
                    fontSize = 11.sp,
                    color = GymGray
                )
            }

            if (hasGuide) {
                IconButton(onClick = onGuideClick) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = "View Guide", tint = GymRed)
                }
            }
        }
    }
}


// ==========================================
// 6. EXERCISE DETAILED GUIDE SCREEN
// ==========================================

@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    viewModel: FitnessViewModel,
    param: String // name OR category_Chest
) {
    var timerSeconds by remember { mutableStateOf(45) }
    var timerRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (param.startsWith("category_")) {
        val catName = param.removePrefix("category_")
        val exercises by viewModel.allExercises.collectAsState()
        val filtered = exercises.filter { it.category.equals(catName, ignoreCase = true) }

        Scaffold(
            topBar = {
                OptTopBar(title = "$catName Library", onBack = { navController.popBackStack() })
            }
        ) { pad ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymBlack)
                    .padding(pad)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "DISCOVER PROFESSIONAL FORMS",
                        color = GymRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                items(filtered) { exe ->
                    GlassCardClickable(onClick = { navController.navigate("exercise_detail/${exe.name}") }) {
                        Text(exe.name, fontWeight = FontWeight.Bold, color = GymWhite)
                        Text("Target: ${exe.targetMuscles}", color = GymGray, fontSize = 11.sp)
                        Text("Burn: ${exe.caloriesBurned} kcal/set", color = GymRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val exercises by viewModel.allExercises.collectAsState()
    val matched = exercises.find { it.name.lowercase() == param.lowercase() }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerSeconds > 0 && timerRunning) {
                delay(1000)
                timerSeconds--
            }
            if (timerSeconds == 0) {
                timerRunning = false
                timerSeconds = 45
            }
        }
    }

    Scaffold(
        topBar = {
            OptTopBar(title = matched?.name ?: "Exercise Guide", onBack = { navController.popBackStack() })
        }
    ) { pad2 ->
        if (matched == null) {
            Box(modifier = Modifier.fillMaxSize().background(GymBlack), contentAlignment = Alignment.Center) {
                Text("Error: Exercise guide not found in local library database", color = GymWhite)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymBlack)
                    .padding(pad2)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(GymRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, GymRed, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GymRed)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(matched.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = GymWhite)
                                Text("Category: ${matched.category}", color = GymGray, fontSize = 12.sp)
                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                    Badge(containerColor = GymRed, contentColor = GymWhite) {
                                        Text("${matched.caloriesBurned}kcal per set", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Rest Timer Box
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ACTIVE REST TIMER", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                            Text(
                                "00:${String.format("%02d", timerSeconds)}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = GymWhite,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Row {
                                Button(
                                    onClick = { timerRunning = !timerRunning },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (timerRunning) GymRedDark else GymRed),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (timerRunning) "PAUSE" else "START REST TIMER", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        timerRunning = false
                                        timerSeconds = 45
                                    },
                                    border = BorderStroke(1.dp, GymWhite.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GymWhite)
                                ) {
                                    Text("RESET")
                                }
                            }
                        }
                    }
                }

                // Muscles targeted
                item {
                    Text("TARGET MUSCLES", color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    GlassCard {
                        Text(matched.targetMuscles, color = GymWhite, fontWeight = FontWeight.Bold)
                    }
                }

                // Instructions list
                item {
                    Text("STEP-BY-STEP FORM GUIDELINES", color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    GlassCard {
                        Text(matched.instructions, color = GymWhite, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }

                // Common mistakes
                item {
                    Text("COMMON FORMS MISTAKES", color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    GlassCard(borderColor = Color.Yellow.copy(alpha = 0.4f)) {
                        Row {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(matched.commonMistakes, color = GymWhite, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = GymWhite)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back", tint = GymRed)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkGray)
    )
}


// ==========================================
// 7. INDIAN DIET PLANNER SCREEN
// ==========================================

@Composable
fun DietPlannerScreen(navController: NavController, viewModel: FitnessViewModel) {
    val dietLogs by viewModel.dietLogs.collectAsState()
    val totalCalories by viewModel.caloriesEatenSum.collectAsState()
    val totalProtein by viewModel.proteinEatenSum.collectAsState()

    var showFoodDialog by remember { mutableStateOf(false) }

    // Add food form states
    var foodName by remember { mutableStateOf("") }
    var foodCalories by remember { mutableStateOf("150") }
    var foodProtein by remember { mutableStateOf("10") }
    var foodCarbs by remember { mutableStateOf("20") }
    var foodFat by remember { mutableStateOf("5") }
    var mealType by remember { mutableStateOf("Breakfast") }

    val context = LocalContext.current

    AppBottomLayout(navController = navController, currentRoute = "diet") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "INDIAN DIET TRACKER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GymWhite
                        )
                    )
                    Text(
                        "Track macros easily. Vegetarian diets friendly.",
                        color = GymGray,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showFoodDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = GymWhite, modifier = Modifier.size(16.dp))
                    Text("ADD MEAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GymWhite, modifier = Modifier.padding(start = 4.dp))
                }
            }

            // Stat summaries
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL CALORIES", fontSize = 9.sp, color = GymGray)
                        Text("$totalCalories", fontSize = 20.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        Text(" kcal", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PROTEIN", fontSize = 9.sp, color = GymGray)
                        Text("${totalProtein.roundToInt()}g", fontSize = 20.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        Text("Grams", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("TODAY'S FOOD LOGS", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            if (dietLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Your daily food tracker is empty. Tap 'ADD MEAL'!", color = GymGray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dietLogs) { item ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Badge(containerColor = GymDarkGray, contentColor = GymRed, modifier = Modifier.padding(bottom = 4.dp)) {
                                        Text(item.mealType.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(item.itemName, fontWeight = FontWeight.Bold, color = GymWhite, fontSize = 14.sp)
                                    Text(
                                        "P: ${item.proteinGrams.roundToInt()}g  C: ${item.carbsGrams.roundToInt()}g  F: ${item.fatGrams.roundToInt()}g",
                                        color = GymGray,
                                        fontSize = 11.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${item.calories} cal", fontWeight = FontWeight.Black, color = GymWhite, modifier = Modifier.padding(end = 12.dp))
                                    IconButton(onClick = { viewModel.removeMealItem(item) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = GymRed)
                                    }
                                }
                            }
                        }
                    }

                    // Diet seed recipes recommendations
                    item {
                        Text("HEALTHY INDIAN HIGH-PROTEIN SUGGESTIONS", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        
                        val suggestions = listOf(
                            Triple("Paneer Bhurji (Veg)", "250kcal • 22g Protein", Triple(22f, 8f, 15f)),
                            Triple("Double Boiled Egg Whites", "68kcal • 14g Protein", Triple(14f, 1f, 0f)),
                            Triple("Roasted Soya Sattu Shake", "320kcal • 20g Protein", Triple(20f, 42f, 4f)),
                            Triple("Classic Soya Chunks dry", "190kcal • 25g Protein", Triple(25f, 15f, 1f))
                        )

                        suggestions.forEach { (name, label, macros) ->
                            val parsedCal = label.takeWhile { it.isDigit() }.toIntOrNull() ?: 200
                            GlassCardClickable(
                                onClick = {
                                    viewModel.addMealItem(name, parsedCal, macros.first, macros.second, macros.third, "Snack")
                                    Toast.makeText(context, "$name added to log!", Toast.LENGTH_SHORT).show()
                                },
                                borderColor = GymWhite.copy(alpha = 0.05f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, color = GymWhite)
                                        Text(label, color = GymGray, fontSize = 11.sp)
                                    }
                                    Text("+ Quick Add", color = GymRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Add Food Dialog
    if (showFoodDialog) {
        AlertDialog(
            onDismissRequest = { showFoodDialog = false },
            title = { Text("Log Daily Meal", color = GymWhite) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Meal Name
                    TextField(value = foodName, onValueChange = { foodName = it }, label = { Text("Food Name") }, colors = TextFieldDefaults.colors(focusedTextColor = GymWhite, focusedContainerColor = GymDarkGray))
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Meal Category Selection", fontSize = 10.sp, color = GymGray)
                    val mealsList = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        mealsList.forEach { m ->
                            val s = mealType == m
                            FilterChip(
                                selected = s,
                                onClick = { mealType = m },
                                label = { Text(m) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calorie input
                    TextField(value = foodCalories, onValueChange = { foodCalories = it }, label = { Text("Calories (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedTextColor = GymWhite))
                    Spacer(modifier = Modifier.height(4.dp))
                    // Protein
                    TextField(value = foodProtein, onValueChange = { foodProtein = it }, label = { Text("Protein (grams)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = TextFieldDefaults.colors(focusedTextColor = GymWhite))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val caloriesInt = foodCalories.trim().toIntOrNull() ?: 150
                        val proteinFl = foodProtein.trim().toFloatOrNull() ?: 10f
                        val carbsFl = foodCarbs.trim().toFloatOrNull() ?: 20f
                        val fatFl = foodFat.trim().toFloatOrNull() ?: 5f
                        
                        if (foodName.isBlank()) {
                            Toast.makeText(context, "Meal name blank!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMealItem(foodName, caloriesInt, proteinFl, carbsFl, fatFl, mealType)
                            showFoodDialog = false
                            foodName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                ) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFoodDialog = false }) { Text("Cancel", color = GymWhite) }
            },
            containerColor = GymDarkGray
        )
    }
}


// ==========================================
// 8. AI COACH CHAT SCREEN
// ==========================================

@Composable
fun ChatCoachScreen(navController: NavController, viewModel: FitnessViewModel) {
    val chatLogs by viewModel.chatLogs.collectAsState()
    val isSending by viewModel.isSendingChat.collectAsState()

    var inputMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AppBottomLayout(navController = navController, currentRoute = "chat") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = GymRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Coach Manu (Gemini AI)", fontWeight = FontWeight.Black, fontSize = 16.sp, color = GymWhite)
                        Text("Ask any Workout, Diet, or Bodybuilding advice", color = GymGray, fontSize = 10.sp)
                    }
                }

                TextButton(onClick = { viewModel.clearChatHistory() }) {
                    Text("Clear Chat", color = GymRed, fontSize = 11.sp)
                }
            }

            // Messages scroll List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chatLogs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = GymGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No conversations yet. Type any query or ask Coach Manu: 'How do I prepare an Indian high-protein fat-cutting vegan plan?' to begin!",
                                color = GymGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(chatLogs) { log ->
                        val isUser = log.sender == "USER"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) GymRed else GymDarkGray
                                ),
                                border = if (isUser) null else BorderStroke(1.dp, GymRed.copy(alpha = 0.2f)),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = log.messageText,
                                        color = GymWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (isSending) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(color = GymRed, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Coach Manu is generating feedback...", color = GymGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputMsg,
                    onValueChange = { inputMsg = it },
                    placeholder = { Text("Ask Coach Manu...", color = GymGray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = GymWhite,
                        focusedContainerColor = GymDarkGray,
                        unfocusedContainerColor = GymDarkGray,
                        focusedIndicatorColor = GymRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMsg.isNotBlank()) {
                            viewModel.sendMessageToAI(inputMsg)
                            inputMsg = ""
                        }
                    },
                    modifier = Modifier
                        .background(GymRed, RoundedCornerShape(12.dp))
                        .size(48.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = "Send Message", tint = GymWhite)
                }
            }
        }
    }
}


// ==========================================
// 9. PROGRESS ANALYTICS SCREEN
// ==========================================

@Composable
fun ProgressAnalyticsScreen(navController: NavController, viewModel: FitnessViewModel) {
    val history by viewModel.progressHistory.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showWeightDialog by remember { mutableStateOf(false) }
    var inputWeight by remember { mutableStateOf("70") }

    val context = LocalContext.current
    val latestHistory = history.lastOrNull()

    Scaffold(
        topBar = {
            OptTopBar(title = "Progress Analytics", onBack = { navController.popBackStack() })
        }
    ) { pad3 ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(pad3)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("BMI METRICS", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                        val bmiVal = latestHistory?.bmi ?: 22.8f
                        Text("$bmiVal", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        val bmiStatus = when {
                            bmiVal < 18.5f -> "Underweight ⚠️"
                            bmiVal < 25f -> "Normal Athletic ✅"
                            bmiVal < 30f -> "Overweight ⚠️"
                            else -> "Obese ⚠️"
                        }
                        Text(bmiStatus, fontSize = 11.sp, color = GymGray, fontWeight = FontWeight.Bold)
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("BODY FAT EST.", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold)
                        val bfVal = latestHistory?.bodyFatEstimate ?: 14.5f
                        Text("$bfVal%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GymWhite)
                        Text("Athletic Tier", fontSize = 11.sp, color = GymGray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real visual dynamic chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("WEIGHT LOGS (PAST 7 ENTRIES)", fontSize = 10.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    CustomWeightGraph(
                        history = history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            // Log entry button trigger
            item {
                Button(
                    onClick = { showWeightDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = GymWhite, modifier = Modifier.size(16.dp))
                    Text("LOG WEIGHT PROGRESS", fontWeight = FontWeight.Bold, color = GymWhite, modifier = Modifier.padding(start = 8.dp))
                }
            }

            // History Log row list
            item {
                Text("HISTORIC ACTIVITY PROGRESS LOGS", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
            }

            if (history.isEmpty()) {
                item {
                    Text("No logs yet. Log your weight to start your tracking timeline!", color = GymGray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                }
            } else {
                items(history.reversed()) { log ->
                    val dateS = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dateS, fontWeight = FontWeight.Bold, color = GymWhite, fontSize = 14.sp)
                                Text("BMI: ${log.bmi} • Body Fat: ${log.bodyFatEstimate}%", color = GymGray, fontSize = 11.sp)
                            }
                            Text("${log.weight} kg", fontWeight = FontWeight.Black, color = GymRed, fontSize = 18.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Weight logging dialog
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Log Weight Entry", color = GymWhite) },
            text = {
                Column {
                    Text("Enter your current weight in kg. BMI and Body Fat Estimate will automatically calibrate based on height/age.", color = GymGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
                    TextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedTextColor = GymWhite, focusedContainerColor = GymDarkGray)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = inputWeight.trim().toFloatOrNull()
                        if (wt == null) {
                            Toast.makeText(context, "Invalid input weight", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.logProgress(wt)
                            showWeightDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRed)
                ) {
                    Text("RECORD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) { Text("Cancel", color = GymWhite) }
            },
            containerColor = GymDarkGray
        )
    }
}


// ==========================================
// 10. COMMUNITY FEED SCREEN
// ==========================================

@Composable
fun CommunityScreen(navController: NavController, viewModel: FitnessViewModel) {
    val posts by viewModel.communityPosts.collectAsState()
    var inputPostText by remember { mutableStateOf("") }
    val context = LocalContext.current

    AppBottomLayout(navController = navController, currentRoute = "community") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "GYM TOWN COMMUNITY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GymWhite
                        )
                    )
                    Text(
                        "Share transformations and stay motivated!",
                        color = GymGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Create Post writing box
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = inputPostText,
                        onValueChange = { inputPostText = it },
                        placeholder = { Text("Share progress or ask gym-goers...", color = GymGray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = GymWhite,
                            focusedContainerColor = GymDarkGray,
                            unfocusedContainerColor = GymDarkGray,
                            focusedIndicatorColor = GymRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputPostText.isNotBlank()) {
                                viewModel.writeCommunityPost(inputPostText)
                                inputPostText = ""
                                Toast.makeText(context, "Post shared successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(GymRed, RoundedCornerShape(10.dp))
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Post", tint = GymWhite, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("TOWN STREAM TIMELINE", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            // Post list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { post ->
                    GlassCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(GymRed, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            post.username.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = GymWhite,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(post.username, fontWeight = FontWeight.Bold, color = GymWhite, fontSize = 14.sp)
                                        Text(post.userTitle, color = GymRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Text(post.dateString, color = GymGray, fontSize = 10.sp)
                            }

                            Text(
                                text = post.content,
                                color = GymWhite,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp),
                                lineHeight = 18.sp
                            )

                            // Footer interaction panel (Like count, Comments count)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Like triggers
                                Row(
                                    modifier = Modifier
                                        .clickable { viewModel.toggleLikePost(post) }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (post.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.likedByMe) GymRed else GymGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${post.likes} Likes",
                                        color = if (post.likedByMe) GymRed else GymGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Comments", tint = GymGray, modifier = Modifier.size(16.dp))
                                    Text("0 Comments", color = GymGray, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


// ==========================================
// 11. SUBSCRIPTION PRICING SCREEN
// ==========================================

@Composable
fun SubscriptionScreen(navController: NavController, viewModel: FitnessViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            OptTopBar(title = "Go Premium Membership", onBack = { navController.popBackStack() })
        }
    ) { pad4 ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(pad4)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(GymRedDark, GymBlack)),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, GymRed, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .background(GymWhite.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("GYM TOWN ROYAL 👑", color = GymWhite, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "UNLEASH THE ROYAL ATHLETE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GymWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Get bespoke daily AI plans, unlimited historical BMI analytics, direct chat with Head Coach Manu, and completely ad-free modes.",
                            color = GymLightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Text("CHOOSE TIER PLAN", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
            }

            // Options packages
            item {
                val premiumActive = profile.isPremium
                val subType = profile.subscriptionType
                
                GlassCard(borderColor = if (premiumActive && subType.contains("Monthly")) Color.Green else GymRed.copy(alpha = 0.3f)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("ROYAL MONTHLY PLAN", fontWeight = FontWeight.Black, color = GymWhite, fontSize = 16.sp)
                                Text("A perfect short coaching block", color = GymGray, fontSize = 11.sp)
                            }
                            Text("Rs. 799/mo", fontWeight = FontWeight.Black, color = GymRed, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (premiumActive && subType.contains("Monthly")) {
                            Button(
                                onClick = { viewModel.cancelPremium() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GymDarkGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CANCEL PREMIUM STATUS", color = GymRed, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.buyPremium("Monthly")
                                    Toast.makeText(context, "Royal Membership activated!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("START 7-DAY TRIAL", fontWeight = FontWeight.Bold, color = GymWhite)
                            }
                        }
                    }
                }
            }

            item {
                val premiumActive = profile.isPremium
                val subType = profile.subscriptionType

                GlassCard(borderColor = if (premiumActive && subType.contains("Yearly")) Color.Green else GymRed.copy(alpha = 0.5f)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ROYAL YEARLY PASS", fontWeight = FontWeight.Black, color = GymWhite, fontSize = 16.sp)
                                    Badge(containerColor = GymRed, contentColor = GymWhite, modifier = Modifier.padding(start = 8.dp)) {
                                        Text("BEST VALUE", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Comprehensive physical transformation", color = GymGray, fontSize = 11.sp)
                            }
                            Text("Rs. 4,999/yr", fontWeight = FontWeight.Black, color = GymRed, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (premiumActive && subType.contains("Yearly")) {
                            Button(
                                onClick = { viewModel.cancelPremium() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GymDarkGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CANCEL PREMIUM STATUS", color = GymRed, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.buyPremium("Yearly")
                                    Toast.makeText(context, "Royal Yearly Pass activated!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("PURCHASE YEARLY ACCESS", fontWeight = FontWeight.Bold, color = GymWhite)
                            }
                        }
                    }
                }
            }

            // Comparative checks table
            item {
                Text("MEMBER ADVANTAGES COMPARISON", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
            }

            item {
                val comparisons = listOf(
                    "Standard Gym Routines Exercises" to Pair("Free ✅", "Royal ✅"),
                    "AI Coach Interaction" to Pair("Fallback ⚠️", "Real-Time Flash ✅"),
                    "Continuous BMI & Fat Analytics" to Pair("No ❌", "Unlimited ✅"),
                    "Ad-Free Workout UI Mode" to Pair("No ❌", "Ad-Free ✅")
                )

                comparisons.forEach { (feature, pair) ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(feature, fontWeight = FontWeight.Bold, color = GymWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Row {
                                Text(pair.first, color = GymGray, fontSize = 11.sp, modifier = Modifier.padding(end = 12.dp))
                                Text(pair.second, color = GymRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ==========================================
// 12. PROFILE SETTINGS SCREEN
// ==========================================

@Composable
fun ProfileScreen(navController: NavController, viewModel: FitnessViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            OptTopBar(title = "Athlete Profile Settings", onBack = { navController.popBackStack() })
        }
    ) { pad5 ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GymBlack)
                .padding(pad5)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Meta Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GymDarkGray, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(GymRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GymWhite, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ATHLETE #${(profile.weight * profile.height).roundToInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = GymWhite)
                        val badgeTier = if (profile.isPremium) "ROYAL MEMBER 👑" else "FREE CHAMPION"
                        Text(badgeTier, color = GymRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            item {
                Text("ONBOARDING DATA VALUES (TAILOR)", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
            }

            // Values list card display
            item {
                GlassCard {
                    Column {
                        ProfileInfoRow("Age", "${profile.age} Years")
                        HorizontalDivider(color = GymWhite.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow("Current Height", "${profile.height.roundToInt()} cm")
                        HorizontalDivider(color = GymWhite.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow("Goal Target", profile.fitnessGoal)
                        HorizontalDivider(color = GymWhite.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow("Experience split", profile.experienceLevel)
                        HorizontalDivider(color = GymWhite.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow("Environment Preference", profile.workoutPreference)
                    }
                }
            }

            // Quick reset and premium actions triggers
            item {
                Text("QUICK MEMBER CONTROLS", fontSize = 11.sp, color = GymRed, fontWeight = FontWeight.Bold)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Purchase Premium trigger
                    if (!profile.isPremium) {
                        Button(
                            onClick = { navController.navigate("subscription") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GymRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("GO ROYAL MEMBERSHIP 👑", fontWeight = FontWeight.Bold, color = GymWhite)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.cancelPremium() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GymDarkGray),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("CANCEL ROYAL PREMIUM STATUS", fontWeight = FontWeight.Bold, color = GymWhite)
                        }
                    }

                    // Reset questionnaire
                    OutlinedButton(
                        onClick = {
                            navController.navigate("onboarding") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                            Toast.makeText(context, "Resumed onboarding setup questionnaire!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GymWhite.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymWhite)
                    ) {
                        Text("RESET ONBOARDING SURVEY", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = GymGray, fontSize = 13.sp)
        Text(value, color = GymWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
