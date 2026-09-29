package com.esh.offlineai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AIUiState
import com.esh.offlineai.AIViewModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReplyScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val uiState by aiViewModel.uiState.collectAsState()
    var receivedMessage by remember { mutableStateOf("") }
    var responsePoints by remember { mutableStateOf("") }

    // Reset state when the screen is shown
    LaunchedEffect(Unit) {
        aiViewModel.reset()
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LiquidTopBar(
                title = stringResource(R.string.feature_reply_title),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = receivedMessage,
                    onValueChange = { receivedMessage = it.toString() },
                    label = { Text(stringResource(R.string.reply_input_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5,
                    colors = liquidTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = responsePoints,
                    onValueChange = { responsePoints = it.toString() },
                    label = { Text(stringResource(R.string.reply_notes_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3,
                    colors = liquidTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidActionButton(
                    text = stringResource(R.string.reply_button_text),
                    isLoading = uiState is AIUiState.Loading,
                    enabled = receivedMessage.isNotBlank() && uiState !is AIUiState.Loading,
                    onClick = { aiViewModel.generateSmartReply(receivedMessage, responsePoints) }
                )

                when (val state = uiState) {
                    is AIUiState.Success -> {
                        ResultCard(output = state.output)
                    }
                    is AIUiState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error: ${state.message}", color = LiquidError)
                    }
                    else -> {}
                }
            }
        }
    }
}
