package com.esh.offlineai

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

data class DownloadState(
    val isModelReady: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadedMb: Float = 0f,
    val totalMb: Float = 0f,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val needsModelUpgrade: Boolean = false,
    val selectedTier: AiModel = AiModel.LITE,
    val activeModel: AiModel = AiModel.LITE
)

class ModelSetupViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ModelSetupVM"
        private const val MIN_MODEL_SIZE_BYTES = 50_000_000L
    }

    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state

    private val workManager = WorkManager.getInstance(application)

    init {
        checkAndInitialize()
    }

    fun checkAndInitialize() {
        val app = getApplication<Application>()
        if (ModelDownloadManager.isAnyModelDownloaded(app)) {
            val active = ModelDownloadManager.getActiveModel(app)
            if (isModelFileValid(app, active)) {
                initializeEngine(app, active)
            } else {
                // Check if other model exists
                val other = if (active == AiModel.LITE) AiModel.PRO else AiModel.LITE
                if (isModelFileValid(app, other)) {
                    ModelDownloadManager.setActiveModel(app, other)
                    initializeEngine(app, other)
                }
            }
        } else if (ModelDownloadManager.hasOldModel(app)) {
            // Old legacy model exists but neither 0.5b-instruct nor 1.5b exists
            _state.value = _state.value.copy(needsModelUpgrade = true)
        }
    }

    private fun isModelFileValid(context: android.content.Context, model: AiModel): Boolean {
        val modelFile = File(ModelDownloadManager.getModelPath(context, model))
        return modelFile.exists() && modelFile.length() > MIN_MODEL_SIZE_BYTES
    }

    fun selectTier(model: AiModel) {
        _state.value = _state.value.copy(selectedTier = model)
    }

    fun startDownload(model: AiModel = _state.value.selectedTier) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString(ModelDownloadWorker.KEY_MODEL_ID, model.id)
            .build()

        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueue(request)

        _state.value = _state.value.copy(
            isDownloading = true,
            isError = false,
            needsModelUpgrade = false,
            selectedTier = model
        )

        workManager.getWorkInfoByIdLiveData(request.id).observeForever(object : Observer<WorkInfo?> {
            override fun onChanged(workInfo: WorkInfo?) {
                if (workInfo == null) return

                val progress = workInfo.progress
                val downloaded = progress.getLong(ModelDownloadWorker.PROGRESS, 0L)
                val total = progress.getLong(ModelDownloadWorker.TOTAL, 0L)

                _state.value = _state.value.copy(
                    downloadedMb = downloaded / (1024f * 1024f),
                    totalMb = if (total > 0) total / (1024f * 1024f) else model.estimatedSizeMb
                )

                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    initializeEngine(getApplication(), model)
                    workManager.getWorkInfoByIdLiveData(request.id).removeObserver(this)
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    val workerError = workInfo.outputData.getString("Error")
                    _state.value = _state.value.copy(
                        isDownloading = false,
                        isError = true,
                        errorMessage = workerError ?: "Download failed. Please check your connection and try again."
                    )
                    workManager.getWorkInfoByIdLiveData(request.id).removeObserver(this)
                }
            }
        })
    }

    fun switchModel(model: AiModel): Boolean {
        val app = getApplication<Application>()
        if (!ModelDownloadManager.isModelDownloaded(app, model)) return false

        ModelDownloadManager.setActiveModel(app, model)
        val success = AIEngine.switchModel(ModelDownloadManager.getModelPath(app, model))
        if (success) {
            _state.value = _state.value.copy(activeModel = model, selectedTier = model)
        }
        return success
    }

    fun deleteModel(model: AiModel): Boolean {
        val app = getApplication<Application>()
        val deleted = ModelDownloadManager.deleteModel(app, model)
        if (deleted && _state.value.activeModel == model) {
            // Fallback to remaining model if available
            val remaining = ModelDownloadManager.getDownloadedModels(app).firstOrNull()
            if (remaining != null) {
                switchModel(remaining)
            } else {
                _state.value = _state.value.copy(isModelReady = false)
            }
        }
        return deleted
    }

    private fun initializeEngine(context: android.content.Context, model: AiModel) {
        val modelPath = ModelDownloadManager.getModelPath(context, model)
        try {
            val success = if (AIEngine.isLoaded()) {
                AIEngine.switchModel(modelPath)
            } else {
                AIEngine.init(modelPath)
            }
            if (success) {
                ModelDownloadManager.setActiveModel(context, model)
                _state.value = _state.value.copy(
                    isModelReady = true,
                    isDownloading = false,
                    isError = false,
                    activeModel = model,
                    selectedTier = model
                )
            } else {
                _state.value = _state.value.copy(
                    isModelReady = false,
                    isDownloading = false,
                    isError = true,
                    errorMessage = "Failed to load ${model.displayName}. Please retry."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error", e)
            _state.value = _state.value.copy(
                isModelReady = false,
                isDownloading = false,
                isError = true,
                errorMessage = "Engine error: ${e.message}"
            )
        }
    }
}
