package com.esh.offlineai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esh.offlineai.AIUiState
import com.esh.offlineai.AIViewModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val uiState by aiViewModel.uiState.collectAsState()

    var toInput by remember { mutableStateOf("") }
    var subjectInput by remember { mutableStateOf("") }
    var rawNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        aiViewModel.reset()
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LiquidTopBar(
                title = stringResource(R.string.feature_email_title),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = toInput,
                    onValueChange = { toInput = it },
                    label = { Text(stringResource(R.string.email_to_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = liquidTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text(stringResource(R.string.email_subject_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = liquidTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rawNotes,
                    onValueChange = { rawNotes = it.toString() },
                    label = { Text(stringResource(R.string.email_notes_label), color = LiquidTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 8,
                    colors = liquidTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidActionButton(
                    text = stringResource(R.string.email_button_text),
                    isLoading = uiState is AIUiState.Loading,
                    enabled = rawNotes.isNotBlank() && uiState !is AIUiState.Loading,
                    onClick = { aiViewModel.generateEmail(toInput, subjectInput, rawNotes) }
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
