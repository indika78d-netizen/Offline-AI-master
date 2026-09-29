package com.esh.offlineai.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AIUiState
import com.esh.offlineai.AIViewModel
import com.esh.offlineai.OcrHelper
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRefinerScreen(aiViewModel: AIViewModel, onBack: () -> Unit) {
    val uiState by aiViewModel.uiState.collectAsState()
    val textInput by aiViewModel.textInput.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isOcrScanning by remember { mutableStateOf(false) }

    val refinementOptions = mapOf(
        "Summarize" to stringResource(R.string.refiner_option_summarize),
        "Fix Grammar & Spelling" to stringResource(R.string.refiner_option_grammar),
        "Make it a Formal Blog Post" to stringResource(R.string.refiner_option_blog),
        "Create Meeting Minutes" to stringResource(R.string.refiner_option_minutes)
    )
    var selectedOption by remember { mutableStateOf(refinementOptions.values.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Reset state when the screen is shown
    LaunchedEffect(Unit) {
        aiViewModel.reset()
        aiViewModel.onTextInputChanged("")
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { aiViewModel.loadTextFromFile(context, it) }
    }

    // Neural OCR Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isOcrScanning = true
                try {
                    val extracted = OcrHelper.extractText(context, it)
                    if (extracted.isNotBlank()) {
                        aiViewModel.onTextInputChanged(extracted)
                        Toast.makeText(context, "Text extracted with Neural OCR!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No text detected in image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isOcrScanning = false
                }
            }
        }
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Liquid Top Bar ───
            LiquidTopBar(
                title = stringResource(R.string.refiner_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Filled.DocumentScanner,
                            contentDescription = "Scan Document (Neural OCR)",
                            tint = LiquidCyan
                        )
                    }
                    IconButton(onClick = {
                        filePickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                    }) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = stringResource(R.string.upload_button),
                            tint = LiquidElectricBlue
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { aiViewModel.onTextInputChanged(it) },
                    label = { Text(stringResource(R.string.refiner_input_label), color = LiquidTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    maxLines = 10,
                    colors = liquidTextFieldColors()
                )

                if (isOcrScanning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LiquidShimmer(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Scanning image with on-device Neural OCR…",
                        fontSize = 12.sp,
                        color = LiquidCyan,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action row for quick document importing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        label = { Text("📷 Scan Notes / OCR", fontSize = 12.sp, color = LiquidTextPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = LiquidCyan
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = LiquidSurface2.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, LiquidCyan.copy(alpha = 0.2f))
                    )

                    AssistChip(
                        onClick = { filePickerLauncher.launch(arrayOf("application/pdf", "text/plain")) },
                        label = { Text("📄 Attach PDF / Text", fontSize = 12.sp, color = LiquidTextPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = LiquidElectricBlue
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = LiquidSurface2.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, LiquidElectricBlue.copy(alpha = 0.2f))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.refiner_advice),
                    fontSize = 12.sp,
                    color = LiquidTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.refiner_task_label), color = LiquidTextMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = liquidTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        refinementOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedOption = label
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LiquidActionButton(
                    text = stringResource(R.string.refiner_button_text),
                    isLoading = uiState is AIUiState.Loading,
                    enabled = uiState !is AIUiState.Loading && textInput.isNotBlank(),
                    onClick = {
                        val key = refinementOptions.entries.firstOrNull { it.value == selectedOption }?.key ?: "Summarize"
                        aiViewModel.generateRefinedDocument(textInput, key)
                    }
                )

                when (val state = uiState) {
                    is AIUiState.Loading -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        LiquidShimmer(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Refining document on-device…",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = LiquidTextSecondary
                        )
                    }
                    is AIUiState.Success -> {
                        ResultCard(output = state.output)
                    }
                    is AIUiState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = LiquidError,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is AIUiState.Idle -> {}
                }
            }
        }
    }
}
