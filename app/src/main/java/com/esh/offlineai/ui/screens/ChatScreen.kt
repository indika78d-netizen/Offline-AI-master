package com.esh.offlineai.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AIViewModel
import com.esh.offlineai.ChatMessage
import com.esh.offlineai.OcrHelper
import com.esh.offlineai.R
import com.esh.offlineai.TextToSpeechHelper
import com.esh.offlineai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val messages by aiViewModel.chatMessages.collectAsState()
    val isLoading by aiViewModel.chatLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Offline Neural TTS Helper
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // OCR Image Scanner Launcher for Chat
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val extracted = OcrHelper.extractText(context, it)
                    if (extracted.isNotBlank()) {
                        inputText = if (inputText.isBlank()) extracted else "$inputText\n$extracted"
                        Toast.makeText(context, "Scanned text added to input!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No text detected", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Liquid Top Bar ───
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
                            contentDescription = stringResource(R.string.back_button),
                            tint = LiquidCyan
                        )
                    }
                    Text(
                        stringResource(R.string.feature_chat_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = LiquidTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Filled.DocumentScanner,
                            contentDescription = "Scan Image Text",
                            tint = LiquidCyan
                        )
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = {
                            ttsHelper.stop()
                            aiViewModel.clearChat()
                        }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.chat_clear),
                                tint = LiquidError.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // ─── Chat Messages ───
            if (messages.isEmpty() && !isLoading) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        LiquidGlowRing(size = 80.dp, color = LiquidCyan) {
                            Text("💬", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.chat_empty_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = LiquidTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.chat_empty_subtitle),
                            fontSize = 14.sp,
                            color = LiquidTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { message ->
                        LiquidChatBubble(
                            message = message,
                            onSpeak = { text ->
                                if (isSpeaking) ttsHelper.stop() else ttsHelper.speak(text)
                            },
                            isSpeaking = isSpeaking
                        )
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Liquid pulsing dots
                                val infiniteTransition = rememberInfiniteTransition(label = "thinking")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = EaseInOut),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "thinking_alpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LiquidCyan.copy(alpha = alpha))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LiquidElectricBlue.copy(alpha = alpha * 0.8f))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LiquidPurple.copy(alpha = alpha * 0.6f))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    stringResource(R.string.chat_thinking),
                                    fontSize = 13.sp,
                                    color = LiquidTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ─── Liquid Input Bar ───
            Surface(
                color = LiquidSurface1.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Top glow line
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    LiquidCyan.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 4.dp.toPx()
                            ),
                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .imePadding()
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.chat_input_hint),
                                color = LiquidTextMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidCyan.copy(alpha = 0.5f),
                            unfocusedBorderColor = LiquidSurface3,
                            focusedContainerColor = LiquidSurface2.copy(alpha = 0.5f),
                            unfocusedContainerColor = LiquidSurface2.copy(alpha = 0.3f),
                            cursorColor = LiquidCyan
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                aiViewModel.sendChatMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = LiquidElectricBlue,
                            contentColor = Color.White,
                            disabledContainerColor = LiquidSurface3,
                            disabledContentColor = LiquidTextMuted
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chat_send))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidChatBubble(
    message: ChatMessage,
    onSpeak: (String) -> Unit,
    isSpeaking: Boolean
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .border(
                    width = 1.dp,
                    brush = if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(
                                LiquidElectricBlue.copy(alpha = 0.3f),
                                LiquidPurple.copy(alpha = 0.15f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = 0.15f),
                                LiquidSurface3.copy(alpha = 0.3f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                ),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    LiquidElectricBlue.copy(alpha = 0.2f)
                else
                    LiquidSurface2.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = LiquidTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )

                // Voice read-out button for assistant replies
                if (!isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = { onSpeak(message.content) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = LiquidCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
