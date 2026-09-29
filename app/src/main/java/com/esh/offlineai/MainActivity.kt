package com.esh.offlineai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esh.offlineai.ui.screens.CaptionScreen
import com.esh.offlineai.ui.screens.ChatScreen
import com.esh.offlineai.ui.screens.CreativeWriterScreen
import com.esh.offlineai.ui.screens.DocumentRefinerScreen
import com.esh.offlineai.ui.screens.EmailScreen
import com.esh.offlineai.ui.screens.HelpScreen
import com.esh.offlineai.ui.screens.HomeScreen
import com.esh.offlineai.ui.screens.ModelSetupScreen
import com.esh.offlineai.ui.screens.SmartReplyScreen
import com.esh.offlineai.ui.screens.TranslatorScreen
import com.esh.offlineai.ui.theme.OfflineAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge for the liquid background to extend behind system bars
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OfflineAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationRouter()
                }
            }
        }
    }
}

@Composable
fun AppNavigationRouter() {
    val setupViewModel: ModelSetupViewModel = viewModel()
    val downloadState by setupViewModel.state.collectAsState()

    if (!downloadState.isModelReady) {
        ModelSetupScreen(
            downloadedMb = downloadState.downloadedMb,
            totalMb = downloadState.totalMb,
            isDownloading = downloadState.isDownloading,
            isError = downloadState.isError,
            errorMessage = downloadState.errorMessage,
            needsModelUpgrade = downloadState.needsModelUpgrade,
            selectedTier = downloadState.selectedTier,
            onSelectTier = { tier -> setupViewModel.selectTier(tier) },
            onStartDownload = { setupViewModel.startDownload() }
        )
        return
    }

    val appViewModel: AppViewModel = viewModel()
    val aiViewModel: AIViewModel = viewModel()
    val currentScreen by appViewModel.currentScreen.collectAsState()
    val context = LocalContext.current

    // Observe the toast event from the shared ViewModel
    LaunchedEffect(Unit) {
        aiViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    when (currentScreen) {
        "home" -> HomeScreen(setupViewModel = setupViewModel, onNavigate = { route -> appViewModel.navigateTo(route) })
        "help" -> HelpScreen(setupViewModel = setupViewModel, onBack = { appViewModel.navigateTo("home") })
        "chat" -> ChatScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "refiner" -> DocumentRefinerScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "reply" -> SmartReplyScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "translator" -> TranslatorScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "creative" -> CreativeWriterScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "email" -> EmailScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        "caption" -> CaptionScreen(aiViewModel = aiViewModel, onBack = { appViewModel.navigateTo("home") })
        else -> HomeScreen(setupViewModel = setupViewModel, onNavigate = { route -> appViewModel.navigateTo(route) })
    }
}
