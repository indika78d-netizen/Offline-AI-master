package com.esh.offlineai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AiModel
import com.esh.offlineai.ModelSetupViewModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

data class Feature(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector = Icons.Default.AutoAwesome,
    val accentColor: Color = LiquidCyan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    setupViewModel: ModelSetupViewModel,
    onNavigate: (String) -> Unit
) {
    var showModelDialog by remember { mutableStateOf(false) }
    val downloadState by setupViewModel.state.collectAsState()
    val activeModel = downloadState.activeModel

    if (showModelDialog) {
        ModelSwitcherDialog(
            setupViewModel = setupViewModel,
            onDismiss = { showModelDialog = false }
        )
    }

    val features = listOf(
        Feature("chat", stringResource(R.string.feature_chat_title), stringResource(R.string.feature_chat_subtitle), Icons.Default.ChatBubble, LiquidCyan),
        Feature("refiner", stringResource(R.string.feature_refiner_title), stringResource(R.string.feature_refiner_subtitle), Icons.Default.Description, LiquidElectricBlue),
        Feature("reply", stringResource(R.string.feature_reply_title), stringResource(R.string.feature_reply_subtitle), Icons.Default.Reply, LiquidPurple),
        Feature("translator", stringResource(R.string.feature_translator_title), stringResource(R.string.feature_translator_subtitle), Icons.Default.Translate, LiquidMint),
        Feature("creative", stringResource(R.string.feature_creative_title), stringResource(R.string.feature_creative_subtitle), Icons.Default.Palette, LiquidMagenta),
        Feature("email", stringResource(R.string.feature_email_title), stringResource(R.string.feature_email_subtitle), Icons.Default.Email, LiquidAmber),
        Feature("caption", stringResource(R.string.feature_caption_title), stringResource(R.string.feature_caption_subtitle), Icons.Default.Tag, LiquidSkyBlue)
    )

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top Bar ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Model Tier Badge (tappable)
                Surface(
                    onClick = { showModelDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    LiquidCyan.copy(alpha = 0.4f),
                                    LiquidPurple.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    LiquidCyan.copy(alpha = 0.08f),
                                    LiquidPurple.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Switch Model Tier",
                            modifier = Modifier.size(14.dp),
                            tint = LiquidCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeModel == AiModel.PRO) "🚀 Pro" else "⚡ Lite",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = LiquidTextPrimary
                        )
                    }
                }

                // Help icon
                IconButton(onClick = { onNavigate("help") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = stringResource(R.string.help_button),
                        tint = LiquidCyan
                    )
                }
            }

            // ─── Header ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated liquid logo
                LiquidGlowRing(size = 72.dp, color = LiquidCyan) {
                    Text("🧠", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = LiquidAccentGradient
                    )
                )
                Text(
                    text = stringResource(R.string.home_title),
                    fontSize = 13.sp,
                    color = LiquidTextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            // ─── Feature Grid ───
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(features) { feature ->
                    LiquidFeatureCard(feature = feature, onClick = { onNavigate(feature.route) })
                }
            }

            // ─── Bottom tagline ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒 100% Private · 📵 Runs Offline",
                    fontSize = 11.sp,
                    color = LiquidTextMuted
                )
            }
        }
    }
}

@Composable
fun LiquidFeatureCard(feature: Feature, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_${feature.route}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        feature.accentColor.copy(alpha = 0.2f),
                        feature.accentColor.copy(alpha = 0.05f),
                        feature.accentColor.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidNavyBlue.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Subtle left-edge glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                feature.accentColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(0f, size.height / 2),
                            radius = size.height
                        ),
                        radius = size.height,
                        center = Offset(0f, size.height / 2)
                    )
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                feature.accentColor.copy(alpha = 0.2f),
                                feature.accentColor.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        feature.accentColor.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feature.subtitle,
                    fontSize = 12.sp,
                    color = LiquidTextSecondary,
                    lineHeight = 16.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LiquidTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
