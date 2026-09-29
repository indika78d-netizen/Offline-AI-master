package com.esh.offlineai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AiModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

@Composable
fun ModelSetupScreen(
    downloadedMb: Float,
    totalMb: Float,
    isDownloading: Boolean,
    isError: Boolean,
    errorMessage: String,
    needsModelUpgrade: Boolean = false,
    selectedTier: AiModel = AiModel.LITE,
    onSelectTier: (AiModel) -> Unit = {},
    onStartDownload: () -> Unit
) {
    val progress = if (totalMb > 0f) downloadedMb / totalMb else 0f
    val progressPercent = (progress * 100).toInt()

    LiquidBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo
            LiquidGlowRing(size = 80.dp, color = LiquidCyan) {
                Text("🧠", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp,
                style = MaterialTheme.typography.displayMedium.copy(
                    brush = LiquidAccentGradient
                )
            )
            Text(
                text = stringResource(R.string.home_title),
                fontSize = 14.sp,
                color = LiquidCyan,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main content card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (isError) LiquidError else LiquidCyan
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDownloading) {
                        // ── Download Progress ──
                        Text(
                            text = if (progress >= 1f) stringResource(R.string.setup_ready) else "Downloading ${selectedTier.displayName}…",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidTextPrimary
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom liquid progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(LiquidSurface3)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LiquidAccentGradient)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${"%.1f".format(downloadedMb)} MB",
                                fontSize = 13.sp,
                                color = LiquidTextSecondary
                            )
                            Text(
                                text = "$progressPercent%",
                                fontSize = 13.sp,
                                color = LiquidCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${"%.1f".format(totalMb)} MB",
                                fontSize = 13.sp,
                                color = LiquidTextSecondary
                            )
                        }

                        if (progress >= 1f) {
                            Spacer(Modifier.height(12.dp))
                            LiquidShimmer(modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.setup_initializing),
                                fontSize = 13.sp,
                                color = LiquidCyan
                            )
                        }
                    } else if (isError) {
                        // ── Error State ──
                        Text(
                            stringResource(R.string.setup_error_title),
                            fontSize = 20.sp,
                            color = LiquidError,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            errorMessage,
                            fontSize = 13.sp,
                            color = LiquidTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidError
                            )
                        ) {
                            Text(stringResource(R.string.setup_retry), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // ── Tier Selection ──
                        Text(
                            text = "Choose Your AI Tier",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidTextPrimary
                        )
                        Text(
                            text = "Select the model that best matches your phone. All models run 100% offline.",
                            fontSize = 13.sp,
                            color = LiquidTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        LiquidTierCard(
                            model = AiModel.LITE,
                            isSelected = selectedTier == AiModel.LITE,
                            onClick = { onSelectTier(AiModel.LITE) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LiquidTierCard(
                            model = AiModel.PRO,
                            isSelected = selectedTier == AiModel.PRO,
                            onClick = { onSelectTier(AiModel.PRO) }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Download button with liquid gradient
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        LiquidAccentGradient,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Download ${selectedTier.displayName} (~${selectedTier.estimatedSizeMb.toInt()} MB)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚡ 100% Private · 📵 Never uses internet after setup",
                            fontSize = 12.sp,
                            color = LiquidCyan.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidTierCard(
    model: AiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isSelected) LiquidCyan else LiquidTextMuted
    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(LiquidCyan.copy(alpha = 0.6f), LiquidPurple.copy(alpha = 0.3f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(LiquidSurface3, LiquidSurface3)
        )
    }
    val bgColor = if (isSelected) LiquidDarkBlue.copy(alpha = 0.8f) else LiquidNavyBlue.copy(alpha = 0.4f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(width = if (isSelected) 1.5.dp else 1.dp, brush = borderBrush, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isSelected) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidCyan.copy(alpha = 0.06f),
                                    Color.Transparent
                                ),
                                center = Offset(0f, size.height / 2),
                                radius = size.height * 1.2f
                            ),
                            radius = size.height * 1.2f,
                            center = Offset(0f, size.height / 2)
                        )
                    }
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = model.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidTextPrimary
                    )
                    Text(
                        text = "~${model.estimatedSizeMb.toInt()} MB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidCyan
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.tierLabel + " · " + model.minRamRecommendation,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LiquidSkyBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = model.description,
                    fontSize = 12.sp,
                    color = LiquidTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
