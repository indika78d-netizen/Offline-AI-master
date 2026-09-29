package com.esh.offlineai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ModelDownloadManager {

    private const val PREFS_NAME = "offline_ai_model_prefs"
    private const val KEY_ACTIVE_MODEL = "active_model_id"
    private const val OLD_MODEL_FILENAME = "qwen2.5-0.5b-q4_k_m.gguf"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    fun getActiveModel(context: Context): AiModel {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_ACTIVE_MODEL, null)
        if (savedId != null) {
            val model = AiModel.fromId(savedId)
            if (isModelDownloaded(context, model)) {
                return model
            }
        }
        // Fallback: check which model exists on disk
        if (isModelDownloaded(context, AiModel.PRO)) return AiModel.PRO
        return AiModel.LITE
    }

    fun setActiveModel(context: Context, model: AiModel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_MODEL, model.id).apply()
    }

    fun getModelPath(context: Context, model: AiModel = getActiveModel(context)): String {
        val modelDir = File(context.filesDir, "models")
        return File(modelDir, model.filename).absolutePath
    }

    fun isModelDownloaded(context: Context, model: AiModel): Boolean {
        val file = File(context.filesDir, "models/${model.filename}")
        return file.exists() && file.length() > 50_000_000L // Sanity check
    }

    fun isAnyModelDownloaded(context: Context): Boolean {
        return AiModel.entries.any { isModelDownloaded(context, it) }
    }

    fun getDownloadedModels(context: Context): List<AiModel> {
        return AiModel.entries.filter { isModelDownloaded(context, it) }
    }

    fun deleteModel(context: Context, model: AiModel): Boolean {
        val file = File(context.filesDir, "models/${model.filename}")
        return if (file.exists()) {
            file.delete()
        } else false
    }

    /**
     * Checks if an old legacy model exists that should be cleaned up.
     */
    fun hasOldModel(context: Context): Boolean {
        val modelDir = File(context.filesDir, "models")
        return File(modelDir, OLD_MODEL_FILENAME).exists()
    }

    /**
     * Removes the old legacy model file to free up space.
     */
    fun deleteOldModel(context: Context) {
        val modelDir = File(context.filesDir, "models")
        val oldFile = File(modelDir, OLD_MODEL_FILENAME)
        if (oldFile.exists()) {
            oldFile.delete()
        }
    }

    suspend fun downloadModel(
        context: Context,
        model: AiModel,
        onProgress: (downloaded: Long, total: Long) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val modelDir = File(context.filesDir, "models")
                if (!modelDir.exists()) modelDir.mkdirs()

                val modelFile = File(modelDir, model.filename)
                val tempFile = File(modelDir, "${model.filename}.tmp")

                val request = Request.Builder().url(model.downloadUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    onError("HTTP Error: ${response.code}")
                    return@withContext
                }

                val body = response.body ?: run {
                    onError("Empty response body")
                    return@withContext
                }

                val totalBytes = body.contentLength()
                var downloadedBytes = 0L
                var lastUpdate = 0L

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            // Throttle updates to ~10 per second to prevent UI/Worker lag
                            val now = System.currentTimeMillis()
                            if (now - lastUpdate > 100) {
                                onProgress(downloadedBytes, totalBytes)
                                lastUpdate = now
                            }
                        }
                    }
                }

                // Final progress update
                onProgress(downloadedBytes, totalBytes)

                if (tempFile.renameTo(modelFile)) {
                    // Update active model preference to the newly downloaded model
                    setActiveModel(context, model)
                    deleteOldModel(context)
                    onSuccess()
                } else {
                    onError("Failed to finalize model file")
                }

            } catch (e: Exception) {
                onError("Download failed: ${e.localizedMessage}")
            }
        }
    }
}
