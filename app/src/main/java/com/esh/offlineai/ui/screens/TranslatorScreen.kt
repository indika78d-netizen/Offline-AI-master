package com.esh.offlineai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AIUiState
import com.esh.offlineai.AIViewModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val uiState by aiViewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    val languages = listOf(
        "English", "Spanish", "French", "German", "Hindi",
        "Chinese", "Portuguese", "Arabic", "Japanese", "Korean"
    )
    var selectedLanguage by remember { mutableStateOf(languages[0]) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        aiViewModel.reset()
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LiquidTopBar(
                title = stringResource(R.string.feature_translator_title),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text(stringResource(R.string.translator_input_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    maxLines = 8,
                    colors = liquidTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.translator_target_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LiquidTextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLanguage,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = liquidTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LiquidActionButton(
                    text = stringResource(R.string.translator_button_text),
                    isLoading = uiState is AIUiState.Loading,
                    enabled = textInput.isNotBlank() && uiState !is AIUiState.Loading,
                    onClick = { aiViewModel.generateTranslation(textInput, selectedLanguage) }
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
