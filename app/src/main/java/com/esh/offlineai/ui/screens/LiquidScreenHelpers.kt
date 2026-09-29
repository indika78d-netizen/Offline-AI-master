package com.esh.offlineai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.ui.theme.*

/**
 * Shared liquid-styled top bar used across feature screens.
 */
@Composable
fun LiquidTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(
                                LiquidDeepBlue,
                                LiquidDeepBlue.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LiquidCyan
                )
            }
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = LiquidTextPrimary,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
    }
}

/**
 * Standard liquid-styled text field colors.
 */
@Composable
fun liquidTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LiquidCyan.copy(alpha = 0.5f),
    unfocusedBorderColor = LiquidSurface3,
    focusedContainerColor = LiquidSurface2.copy(alpha = 0.3f),
    unfocusedContainerColor = LiquidSurface2.copy(alpha = 0.15f),
    cursorColor = LiquidCyan,
    focusedTextColor = LiquidTextPrimary,
    unfocusedTextColor = LiquidTextPrimary,
    focusedLabelColor = LiquidCyan,
    unfocusedLabelColor = LiquidTextMuted
)

/**
 * Gradient-filled action button with loading spinner.
 */
@Composable
fun LiquidActionButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = LiquidSurface3
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) LiquidAccentGradient
                    else Brush.horizontalGradient(listOf(LiquidSurface3, LiquidSurface3)),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else LiquidTextMuted
                )
            }
        }
    }
}
