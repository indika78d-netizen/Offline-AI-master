package com.esh.offlineai.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ──────────────────────────────────────────────
// 🌊  Liquid UI Components
// ──────────────────────────────────────────────

/**
 * Full-screen animated liquid gradient background with slowly drifting orbs.
 * Use as the root background for every screen.
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_bg")

    // Slowly rotate through phase angles for orb drift
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Deep base gradient
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientDeepStart,
                            GradientLiquidMid,
                            GradientDeepEnd
                        )
                    )
                )

                val rad1 = Math.toRadians(phase1.toDouble())
                val rad2 = Math.toRadians(phase2.toDouble())

                // Liquid orb 1 — cyan
                val orb1x = size.width * 0.3f + (size.width * 0.2f * cos(rad1)).toFloat()
                val orb1y = size.height * 0.25f + (size.height * 0.15f * sin(rad1)).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(orb1x, orb1y),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(orb1x, orb1y)
                )

                // Liquid orb 2 — purple
                val orb2x = size.width * 0.7f + (size.width * 0.15f * cos(rad2)).toFloat()
                val orb2y = size.height * 0.65f + (size.height * 0.1f * sin(rad2)).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidPurple.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(orb2x, orb2y),
                        radius = size.width * 0.45f
                    ),
                    radius = size.width * 0.45f,
                    center = Offset(orb2x, orb2y)
                )

                // Liquid orb 3 — electric blue (subtle top-right glow)
                val orb3x = size.width * 0.85f + (size.width * 0.05f * sin(rad1 * 0.7)).toFloat()
                val orb3y = size.height * 0.1f + (size.height * 0.05f * cos(rad2 * 0.5)).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidElectricBlue.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(orb3x, orb3y),
                        radius = size.width * 0.35f
                    ),
                    radius = size.width * 0.35f,
                    center = Offset(orb3x, orb3y)
                )
            },
        content = content
    )
}

/**
 * Glassmorphism card with blurred translucent background and glowing border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = LiquidCyan,
    borderAlpha: Float = 0.15f,
    fillAlpha: Float = 0.08f,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = borderAlpha * 2),
                        glowColor.copy(alpha = borderAlpha * 0.5f),
                        glowColor.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = LiquidNavyBlue.copy(alpha = 0.65f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = fillAlpha),
                            Color.Transparent,
                            glowColor.copy(alpha = fillAlpha * 0.3f)
                        )
                    )
                )
        ) {
            Column(content = content)
        }
    }
}

/**
 * Animated pulsing glow ring — used around icons / logos.
 */
@Composable
fun LiquidGlowRing(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    color: Color = LiquidCyan,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_ring")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size((size.value * pulseScale).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = pulseAlpha),
                            color.copy(alpha = pulseAlpha * 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Inner content circle
        Box(
            modifier = Modifier
                .size((size.value * 0.75f).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            LiquidNavyBlue.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Gradient accent brush for buttons, highlights.
 */
val LiquidAccentGradient = Brush.horizontalGradient(
    colors = listOf(LiquidElectricBlue, LiquidCyan)
)

val LiquidPurpleCyanGradient = Brush.horizontalGradient(
    colors = listOf(LiquidPurple, LiquidCyan)
)

val LiquidAuroraGradient = Brush.linearGradient(
    colors = listOf(GradientAuroraStart, LiquidCyan, LiquidMint)
)

/**
 * Shimmer / liquid ripple overlay effect for loading states.
 */
@Composable
fun LiquidShimmer(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LiquidSurface2)
            .drawBehind {
                val shimmerWidth = size.width * 0.3f
                val start = size.width * shimmerX
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LiquidCyan.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startX = start,
                        endX = start + shimmerWidth
                    )
                )
            }
    )
}
