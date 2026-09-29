package com.esh.offlineai

import android.util.Log

/**
 * Singleton JNI Wrapper for llama.cpp interaction.
 * Thread-safe initialization with proper state tracking.
 */
object AIEngine {

    private const val TAG = "AIEngine"

    @Volatile
    private var isInitialized = false

    init {
        System.loadLibrary("llama-jni")
    }

    /**
     * Initializes the llama.cpp model from the given path.
     * Thread-safe — only the first call will actually initialize.
     * @param modelPath Absolute path to the .gguf model file.
     * @return true if successful
     */
    @Synchronized
    fun init(modelPath: String): Boolean {
        if (isInitialized) {
            Log.i(TAG, "Engine already initialized, skipping.")
            return true
        }
        return try {
            val result = initModelNative(modelPath)
            if (result) {
                isInitialized = true
                Log.i(TAG, "Engine initialized successfully.")
            } else {
                Log.e(TAG, "Native init returned false for: $modelPath")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Engine init failed: ${e.message}", e)
            false
        }
    }

    /**
     * Unloads current model from memory and loads the specified model.
     * Useful for switching between Lite (0.5B) and Pro (1.5B) tiers.
     */
    @Synchronized
    fun switchModel(modelPath: String): Boolean {
        release()
        return init(modelPath)
    }

    fun isLoaded(): Boolean = isInitialized

    /**
     * Generates a response based on the input prompt.
     * @param prompt User input
     * @param maxTokens Maximum number of tokens to generate
     * @return Model response or error message
     */
    fun generate(prompt: String, maxTokens: Int = 128): String {
        if (!isInitialized) {
            return "Error: AI Engine not initialized. Please restart the app."
        }
        return try {
            generateTextNative(prompt, maxTokens)
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed: ${e.message}", e)
            "Error: Generation failed — ${e.message}"
        }
    }

    /**
     * Releases all native resources. Call when the app is being destroyed.
     */
    @Synchronized
    fun release() {
        if (isInitialized) {
            try {
                freeModelNative()
                isInitialized = false
                Log.i(TAG, "Engine resources released.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to release engine: ${e.message}", e)
            }
        }
    }

    // --- Native Methods ---
    private external fun initModelNative(modelPath: String): Boolean
    private external fun generateTextNative(prompt: String, maxTokens: Int): String
    private external fun freeModelNative()
}
