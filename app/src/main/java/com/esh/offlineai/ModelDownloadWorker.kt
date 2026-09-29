package com.esh.offlineai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val PROGRESS = "Progress"
        const val TOTAL = "Total"
        const val KEY_MODEL_ID = "model_id"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "download_channel"
    }

    override suspend fun doWork(): Result {
        val modelId = inputData.getString(KEY_MODEL_ID)
        val targetModel = AiModel.fromId(modelId)

        try {
            setForeground(getForegroundInfo(targetModel))
        } catch (e: Exception) {
            // Background fallback if foreground can't start
        }

        var errorMsg: String? = null

        return try {
            ModelDownloadManager.downloadModel(
                applicationContext,
                model = targetModel,
                onProgress = { downloaded, total ->
                    setProgressAsync(workDataOf(PROGRESS to downloaded, TOTAL to total))
                    val progressInt = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                    updateNotification(targetModel, progressInt)
                },
                onSuccess = {},
                onError = { 
                    errorMsg = it
                    throw Exception(it) 
                }
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("Error" to (errorMsg ?: e.localizedMessage ?: "Unknown error")))
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val modelId = inputData.getString(KEY_MODEL_ID)
        return getForegroundInfo(AiModel.fromId(modelId))
    }

    private fun getForegroundInfo(model: AiModel): ForegroundInfo {
        return createForegroundInfo(model, 0)
    }

    private fun createForegroundInfo(model: AiModel, progress: Int): ForegroundInfo {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of AI model download"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Downloading ${model.displayName}")
            .setContentText("Getting your offline AI model ready…")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, progress, progress <= 0)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(model: AiModel, progress: Int) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Downloading ${model.displayName}")
            .setContentText("Getting your offline AI model ready…")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, progress, false)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }
}
