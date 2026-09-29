package com.esh.offlineai

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(val role: String, val content: String)

class AIViewModel : ViewModel() {

    companion object {
        /** Max input characters to prevent OOM on very large text pastes */
        private const val MAX_INPUT_CHARS = 1500
    }

    private val _uiState = MutableStateFlow<AIUiState>(AIUiState.Idle)
    val uiState: StateFlow<AIUiState> = _uiState

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput
    
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Chat conversation history
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading

    fun onTextInputChanged(newText: String) {
        _textInput.value = newText
    }

    fun loadTextFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _textInput.value = "Extracting text from file..."
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri)
                val content = when {
                    mimeType == "application/pdf" -> {
                        val text = readTextFromPdf(context, uri)
                        if (text.isBlank()) {
                            _toastEvent.tryEmit("Warning: This PDF appears to contain only images. No text could be extracted.")
                        }
                        text
                    }
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> readTextFromDocx(context, uri)
                    else -> readTextFromPlainText(context, uri)
                }
                _textInput.value = content
            } catch (e: Exception) {
                _uiState.value = AIUiState.Error("Failed to read file: ${e.message}")
                _textInput.value = ""
            }
        }
    }

    private suspend fun readTextFromPlainText(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                reader?.readText() ?: ""
            }
        }
    }

    private suspend fun readTextFromPdf(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            PDFBoxResourceLoader.init(context)
            val inputStream = context.contentResolver.openInputStream(uri)
            val document = PDDocument.load(inputStream)
            val text = PDFTextStripper().getText(document)
            document.close()
            text
        }
    }

    private suspend fun readTextFromDocx(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val zipInputStream = java.util.zip.ZipInputStream(inputStream)
            val textBuilder = StringBuilder()
            
            try {
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val parser = android.util.Xml.newPullParser()
                        parser.setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                        parser.setInput(zipInputStream, null)
                        
                        var eventType = parser.eventType
                        var inTextTag = false
                        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                            when (eventType) {
                                org.xmlpull.v1.XmlPullParser.START_TAG -> {
                                    if (parser.name == "w:t") {
                                        inTextTag = true
                                    }
                                }
                                org.xmlpull.v1.XmlPullParser.TEXT -> {
                                    if (inTextTag) {
                                        textBuilder.append(parser.text)
                                    }
                                }
                                org.xmlpull.v1.XmlPullParser.END_TAG -> {
                                    if (parser.name == "w:t") {
                                        inTextTag = false
                                    } else if (parser.name == "w:p") {
                                        textBuilder.append("\n")
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                        break
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                zipInputStream.close()
            }
            textBuilder.toString().trim()
        }
    }

    /**
     * Truncate input text to prevent OOM on the native side.
     * Returns a warning message if truncation occurred, null otherwise.
     */
    private fun sanitizeInput(text: String): Pair<String, String?> {
        return if (text.length > MAX_INPUT_CHARS) {
            val truncated = text.take(MAX_INPUT_CHARS)
            truncated to "Input was truncated to ${MAX_INPUT_CHARS} characters for performance."
        } else {
            text to null
        }
    }

    /**
     * Build a ChatML prompt string. Qwen3 uses the same ChatML format as Qwen2.5.
     * We use /no_think to skip the thinking mode for faster, direct responses.
     */
    private fun buildPrompt(systemPrompt: String, userMessage: String): String {
        return "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$userMessage /no_think<|im_end|>\n<|im_start|>assistant\n"
    }

    fun generateRefinedDocument(text: String, format: String) {
        val (safeText, warning) = sanitizeInput(text)
        warning?.let { _toastEvent.tryEmit(it) }

        val systemPrompt = when (format) {
            "Summarize" -> "You are a helpful assistant that summarizes text concisely."
            "Fix Grammar & Spelling" -> "You are a meticulous editor. Correct all grammar and spelling mistakes."
            "Make it a Formal Blog Post" -> "You are a professional blog writer. Rewrite the input into a formal blog post."
            "Create Meeting Minutes" -> "Convert the input into structured meeting minutes."
            else -> "You are a helpful assistant."
        }
        generate(buildPrompt(systemPrompt, safeText), 400)
    }

    fun generateSmartReply(receivedMessage: String, userNotes: String) {
        val (safeMessage, _) = sanitizeInput(receivedMessage)
        val systemPrompt = "You are a context-aware assistant. Write a natural, appropriate reply to the message provided, incorporating the user's specific points."
        val userMessage = "Message received: $safeMessage\nMy response points: $userNotes"
        generate(buildPrompt(systemPrompt, userMessage), 256)
    }

    fun generateCreativeContent(topic: String, format: String) {
        val systemPrompt = when (format) {
            "Story Ideas" -> "Generate 3 creative and unique story premises based on the keywords."
            "Poem" -> "Write a beautiful and evocative poem about the given topic."
            "Character Profile" -> "Create a detailed character profile, including name, personality, and background."
            else -> "You are a creative writing assistant."
        }
        generate(buildPrompt(systemPrompt, topic), 300)
    }

    fun generateEmail(to: String, subject: String, notes: String) {
        val systemPrompt = "You are a professional assistant that drafts emails."
        val userMessage = "Write an email to $to about $subject based on these notes: $notes"
        generate(buildPrompt(systemPrompt, userMessage), 256)
    }

    fun generateCaption(description: String) {
        val systemPrompt = "You are a social media expert who writes engaging captions with emojis."
        val userMessage = "Write a short caption for this: $description"
        generate(buildPrompt(systemPrompt, userMessage), 100)
    }

    fun generateTranslation(text: String, targetLanguage: String) {
        val (safeText, warning) = sanitizeInput(text)
        warning?.let { _toastEvent.tryEmit(it) }

        val systemPrompt = "You are a professional translator. Translate the following text accurately into $targetLanguage. Only output the translation, nothing else."
        generate(buildPrompt(systemPrompt, safeText), 400)
    }

    /**
     * Send a message in a multi-turn chat conversation.
     * Builds the full conversation history into the prompt.
     */
    fun sendChatMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val newMessages = _chatMessages.value + ChatMessage("user", userMessage)
        _chatMessages.value = newMessages
        _chatLoading.value = true

        viewModelScope.launch {
            try {
                // Build multi-turn prompt from conversation history
                val prompt = buildChatPrompt(newMessages)
                val result = withContext(Dispatchers.Default) {
                    AIEngine.generate(prompt, 256)
                }
                if (result.startsWith("Error:")) {
                    _chatMessages.value = newMessages + ChatMessage("assistant", result)
                } else {
                    _chatMessages.value = newMessages + ChatMessage("assistant", result.trim())
                }
            } catch (e: Exception) {
                _chatMessages.value = newMessages + ChatMessage("assistant", "Error: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                _chatLoading.value = false
            }
        }
    }

    private fun buildChatPrompt(messages: List<ChatMessage>): String {
        val sb = StringBuilder()
        sb.append("<|im_start|>system\nYou are a helpful, friendly AI assistant. Provide clear, concise answers. /no_think<|im_end|>\n")
        
        // Keep only last few turns to fit in context window
        val recentMessages = if (messages.size > 6) messages.takeLast(6) else messages
        
        for (msg in recentMessages) {
            sb.append("<|im_start|>${msg.role}\n${msg.content}<|im_end|>\n")
        }
        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
        _chatLoading.value = false
    }

    private fun generate(prompt: String, maxTokens: Int) {
        viewModelScope.launch {
            _uiState.value = AIUiState.Loading
            try {
                val result = withContext(Dispatchers.Default) {
                    AIEngine.generate(prompt, maxTokens)
                }
                if (result.startsWith("Error:")) {
                    _uiState.value = AIUiState.Error(result)
                } else {
                    _uiState.value = AIUiState.Success(result.trim())
                }
            } catch (e: Exception) {
                _uiState.value = AIUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _uiState.value = AIUiState.Idle
    }
}

sealed class AIUiState {
    object Idle : AIUiState()
    object Loading : AIUiState()
    data class Success(val output: String) : AIUiState()
    data class Error(val message: String) : AIUiState()
}
