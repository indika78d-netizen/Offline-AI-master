package com.esh.offlineai.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esh.offlineai.AiModel
import com.esh.offlineai.ModelSetupViewModel
import com.esh.offlineai.R
import com.esh.offlineai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    setupViewModel: ModelSetupViewModel? = null,
    onBack: () -> Unit
) {
    var showModelDialog by remember { mutableStateOf(false) }

    if (showModelDialog && setupViewModel != null) {
        ModelSwitcherDialog(
            setupViewModel = setupViewModel,
            onDismiss = { showModelDialog = false }
        )
    }

    LiquidBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            LiquidTopBar(
                title = stringResource(R.string.help_title),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Model Management Quick Card
                if (setupViewModel != null) {
                    val downloadState by setupViewModel.state.collectAsState()
                    val activeModel = downloadState.activeModel

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = LiquidElectricBlue,
                        borderAlpha = 0.2f
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Active Model",
                                    fontSize = 12.sp,
                                    color = LiquidCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeModel.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidTextPrimary
                                )
                                Text(
                                    text = activeModel.tierLabel,
                                    fontSize = 12.sp,
                                    color = LiquidTextSecondary
                                )
                            }

                            Button(
                                onClick = { showModelDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LiquidElectricBlue
                                )
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.help_manage_models_btn), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                LiquidHelpSection(
                    title = stringResource(R.string.help_about_title),
                    content = stringResource(R.string.help_about_desc)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidHelpSection(
                    title = stringResource(R.string.help_tiers_title),
                    content = stringResource(R.string.help_tiers_desc)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidHelpSection(
                    title = stringResource(R.string.help_neural_title),
                    content = stringResource(R.string.help_neural_desc)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.help_faq_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        brush = LiquidAccentGradient
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LiquidFAQItem(
                    question = stringResource(R.string.help_faq_q1),
                    answer = stringResource(R.string.help_faq_a1)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidFAQItem(
                    question = stringResource(R.string.help_faq_q2),
                    answer = stringResource(R.string.help_faq_a2)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidFAQItem(
                    question = stringResource(R.string.help_faq_q3),
                    answer = stringResource(R.string.help_faq_a3)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LiquidHelpSection(title: String, content: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidPurple,
        borderAlpha = 0.1f,
        fillAlpha = 0.04f,
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = LiquidTextSecondary
            )
        }
    }
}

@Composable
fun LiquidFAQItem(question: String, answer: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidElectricBlue,
        borderAlpha = 0.08f,
        fillAlpha = 0.03f,
        cornerRadius = 14.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Q: $question",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = answer,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = LiquidTextSecondary
            )
        }
    }
}
