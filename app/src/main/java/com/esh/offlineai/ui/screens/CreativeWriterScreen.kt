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
fun CreativeWriterScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val uiState by aiViewModel.uiState.collectAsState()
    var topicInput by remember { mutableStateOf("") }

    val creativeOptions = mapOf(
        "Story Ideas" to stringResource(R.string.creative_option_story),
        "Poem" to stringResource(R.string.creative_option_poem),
        "Character Profile" to stringResource(R.string.creative_option_profile)
    )
    var selectedOption by remember { mutableStateOf(creativeOptions.values.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Reset state when the screen is shown
    LaunchedEffect(Unit) {
        aiViewModel.reset()
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LiquidTopBar(
                title = stringResource(R.string.feature_creative_title),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.creative_task_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LiquidTextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Creative Options Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
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
                        creativeOptions.forEach { (_, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = {
                                    selectedOption = value
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text(stringResource(R.string.creative_input_label), color = LiquidTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5,
                    colors = liquidTextFieldColors()
                )

                Spacer(modifier = Modifier.height(24.dp))

                LiquidActionButton(
                    text = stringResource(R.string.creative_button_text),
                    isLoading = uiState is AIUiState.Loading,
                    enabled = topicInput.isNotBlank() && uiState !is AIUiState.Loading,
                    onClick = {
                        val selectedKey = creativeOptions.entries.find { it.value == selectedOption }?.key ?: "Story Ideas"
                        aiViewModel.generateCreativeContent(topicInput, selectedKey)
                    }
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
