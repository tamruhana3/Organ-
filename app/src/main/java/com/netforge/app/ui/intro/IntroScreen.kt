package com.netforge.app.ui.intro

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@Composable
fun IntroScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val page = introPagesList[currentPage]
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "introBlob")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = Motion.easeStandard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = NetForgeInk
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background subtle ambient light
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 40.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                NetForgeAccent.copy(alpha = 0.15f),
                                NetForgeInk.copy(alpha = 0f)
                            )
                        )
                    )
            )

            // Top Bar with Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NETFORGE",
                    style = Typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetForgeAccent
                    )
                )

                if (currentPage < introPagesList.size - 1) {
                    Text(
                        text = "Skip",
                        style = Typography.bodyMedium.copy(color = NetForgeSlate),
                        modifier = Modifier.clickable {
                            Haptics.light(context)
                            onFinish()
                        }
                    )
                }
            }

            // Main Page Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = page.iconEmoji,
                    fontSize = 54.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NetForgePaper2)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = page.badge,
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = NetForgeAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.title,
                    style = Typography.displayLarge,
                    color = NetForgeChalk
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.subtitle,
                    style = Typography.bodyLarge,
                    color = NetForgeSlate,
                    lineHeight = 24.sp
                )
            }

            // Bottom Navigation & Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                // Dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    introPagesList.indices.forEach { index ->
                        val isSelected = index == currentPage
                        val dotWidth = if (isSelected) 24.dp else 8.dp
                        val dotColor = if (isSelected) NetForgeAccent else NetForgeBorder

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Button(
                    onClick = {
                        Haptics.medium(context)
                        if (currentPage < introPagesList.size - 1) {
                            currentPage++
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = if (currentPage == introPagesList.size - 1) "Enter NetForge" else "Continue",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = NetForgeChalk
                    )
                }
            }
        }
    }
}
