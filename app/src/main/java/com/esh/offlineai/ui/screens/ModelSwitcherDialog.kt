package com.esh.offlineai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AiModel
import com.esh.offlineai.ModelDownloadManager
import com.esh.offlineai.ModelSetupViewModel
import com.esh.offlineai.ui.theme.*

@Composable
fun ModelSwitcherDialog(
    setupViewModel: ModelSetupViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadState by setupViewModel.state.collectAsState()
    val activeModel = downloadState.activeModel

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidNavyBlue,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    "AI Model Manager",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LiquidTextPrimary
                )
                Text(
                    "Switch between Lite (Fast) and Pro (Deep Intelligence)",
                    fontSize = 13.sp,
                    color = LiquidTextSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AiModel.entries.forEach { model ->
                    val isDownloaded = ModelDownloadManager.isModelDownloaded(context, model)
                    val isActive = activeModel == model

                    val borderBrush = when {
                        isActive -> Brush.linearGradient(
                            listOf(LiquidCyan.copy(alpha = 0.5f), LiquidPurple.copy(alpha = 0.3f))
                        )
                        isDownloaded -> Brush.linearGradient(
                            listOf(LiquidSurface3, LiquidSurface3)
                        )
                        else -> Brush.linearGradient(
                            listOf(LiquidSurface3.copy(alpha = 0.5f), LiquidSurface3.copy(alpha = 0.5f))
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                brush = borderBrush,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) LiquidDarkBlue.copy(alpha = 0.7f)
                            else LiquidSurface2.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .drawBehind {
                                    if (isActive) {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    LiquidCyan.copy(alpha = 0.06f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(0f, size.height / 2),
                                                radius = size.height
                                            ),
                                            radius = size.height,
                                            center = Offset(0f, size.height / 2)
                                        )
                                    }
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = null,
                                        tint = if (isActive) LiquidCyan else LiquidTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = model.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = LiquidTextPrimary
                                    )
                                }

                                if (isActive) {
                                    Surface(
                                        color = LiquidElectricBlue,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (isDownloaded) {
                                    Surface(
                                        color = LiquidMint.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "READY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidMint,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${model.tierLabel} · ~${model.estimatedSizeMb.toInt()} MB",
                                fontSize = 12.sp,
                                color = LiquidCyan
                            )
                            Text(
                                text = model.description,
                                fontSize = 12.sp,
                                color = LiquidTextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isActive && isDownloaded) {
                                    IconButton(
                                        onClick = {
                                            setupViewModel.deleteModel(model)
                                            Toast.makeText(context, "Deleted ${model.displayName} file", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete model file",
                                            tint = LiquidError
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val ok = setupViewModel.switchModel(model)
                                            if (ok) {
                                                Toast.makeText(context, "Switched to ${model.displayName}", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LiquidElectricBlue
                                        )
                                    ) {
                                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Switch", fontSize = 13.sp)
                                    }
                                } else if (!isDownloaded) {
                                    Button(
                                        onClick = {
                                            setupViewModel.startDownload(model)
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LiquidCyan
                                        )
                                    ) {
                                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = LiquidDeepBlue)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Download (~${model.estimatedSizeMb.toInt()}MB)", fontSize = 13.sp, color = LiquidDeepBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = LiquidCyan)
            }
        }
    )
}
