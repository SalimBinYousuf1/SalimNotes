package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassBox
import com.example.ui.components.StarburstLogo
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ChipSelectedFill
import com.example.ui.theme.ChipSelectedText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.homeGradient

@Composable
fun OnboardingScreen(
    onFinish: (name: String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var enteredName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Step forward after requesting permissions
        onFinish(enteredName.ifBlank { "You" })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeGradient())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                StarburstLogo(size = 64.dp, strokeWidthDp = 4.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Salim",
                    fontFamily = OutfitFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Step Content inside Glass card
            AnimatedContent(targetState = step, label = "onboarding_steps") { currentStep ->
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = GlassBackground,
                    borderColor = GlassBorder,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (currentStep) {
                            0 -> {
                                Text(
                                    text = "Thoughtful Notes,\nPure Simplicity",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Organize thoughts, voice recordings, checklists, and photo stacks in an offline-first glass interface.",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            1 -> {
                                Text(
                                    text = "What should we\ncall you?",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = enteredName,
                                    onValueChange = { enteredName = it },
                                    placeholder = {
                                        Text("Enter your name", color = Color.White.copy(alpha = 0.6f))
                                    },
                                    singleLine = true,
                                    shape = CircleShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                        focusedContainerColor = Color(0x33FFFFFF),
                                        unfocusedContainerColor = Color(0x22FFFFFF)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_name_input")
                                )
                            }
                            2 -> {
                                Text(
                                    text = "Stay on track",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Enable notifications for timely reminders and microphone for voice notes.",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) {
                        Text(
                            text = "Back",
                            fontFamily = OutfitFontFamily,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            val perms = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            perms.add(Manifest.permission.RECORD_AUDIO)
                            permissionLauncher.launch(perms.toTypedArray())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChipSelectedFill,
                        contentColor = ChipSelectedText
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (step == 2) "Get Started" else "Continue",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
