package com.esh.offlineai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.R
import com.esh.offlineai.TextToSpeechHelper
import com.esh.offlineai.ui.theme.*

/**
 * Shared composable for displaying AI generation results with
 * copy-to-clipboard, share functionality, and offline Neural TTS read-aloud.
 * Styled with Liquid skin glassmorphism.
 */
@Composable
fun ResultCard(output: String) {
    val context = LocalContext.current
    val copiedToast = stringResource(R.string.copied_toast)

    // Offline Text-to-Speech instance
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.result_label),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LiquidCyan
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Offline Neural Voice Read-Aloud
            IconButton(onClick = {
                if (isSpeaking) {
                    ttsHelper.stop()
                } else {
                    ttsHelper.speak(output)
                }
            }) {
                Icon(
                    imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop Read Aloud" else "Read Aloud (Offline Voice)",
                    tint = if (isSpeaking) LiquidError else LiquidCyan
                )
            }

            IconButton(onClick = {
                copyToClipboard(context, output)
                Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_button),
                    tint = LiquidElectricBlue
                )
            }

            IconButton(onClick = {
                shareText(context, output)
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.share_button),
                    tint = LiquidPurple
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidCyan,
        borderAlpha = 0.12f,
        fillAlpha = 0.05f,
        cornerRadius = 16.dp
    ) {
        SelectionContainer {
            Text(
                text = output,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp,
                color = LiquidTextPrimary
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("MindSync Result", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
