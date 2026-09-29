package com.esh.offlineai

/**
 * Defines available offline AI models for the Dual Model Tier.
 * - LITE: Qwen2.5-0.5B-Instruct (~398 MB) - Fast, battery efficient, works smoothly on budget devices (2GB+ RAM).
 * - PRO: Qwen2.5-1.5B-Instruct (~986 MB) - Deep reasoning, rich writing, handles complex queries on modern devices (4GB+ RAM).
 */
enum class AiModel(
    val id: String,
    val displayName: String,
    val tierLabel: String,
    val subtitle: String,
    val description: String,
    val filename: String,
    val downloadUrl: String,
    val estimatedSizeMb: Float,
    val minRamRecommendation: String
) {
    LITE(
        id = "qwen2.5-0.5b",
        displayName = "MindSync Lite",
        tierLabel = "⚡ Budget & Fast",
        subtitle = "Qwen 2.5 0.5B (~398 MB)",
        description = "Optimized for budget phones and battery efficiency. Quick summaries, replies, and light tasks.",
        filename = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
        estimatedSizeMb = 398f,
        minRamRecommendation = "2 GB+ RAM"
    ),
    PRO(
        id = "qwen2.5-1.5b",
        displayName = "MindSync Pro",
        tierLabel = "🚀 Premium Intelligence",
        subtitle = "Qwen 2.5 1.5B (~986 MB)",
        description = "Deeper reasoning, sophisticated writing, and higher precision for complex documents and multi-turn chat.",
        filename = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
        estimatedSizeMb = 986f,
        minRamRecommendation = "4 GB - 6 GB+ RAM"
    );

    companion object {
        fun fromId(id: String?): AiModel {
            return entries.find { it.id == id } ?: LITE
        }
    }
}
