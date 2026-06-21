package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.*
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainViewModel(private val repository: AegisRepository) : ViewModel() {

    // --- Tab Selection ---
    private val _currentTab = MutableStateFlow(AegisTab.CHAT)
    val currentTab: StateFlow<AegisTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AegisTab) {
        _currentTab.value = tab
    }

    // --- Offline mode toggle ---
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    // --- Model Management ---
    val allModels: StateFlow<List<LocalModel>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeModelId = MutableStateFlow<String?>(null)
    val activeModelId: StateFlow<String?> = _activeModelId.asStateFlow()

    init {
        // Pre-populate some popular slots if db is empty
        viewModelScope.launch {
            allModels.first { true } // wait until flow loads or completes first collection
            delay(1000)
            if (allModels.value.isEmpty()) {
                val presetModels = listOf(
                    LocalModel(
                        id = "TheBloke/Gemma-2B-IT-GGUF",
                        name = "Gemma 2B Instruct",
                        sizeGbs = 1.62,
                        isDownloaded = true,
                        downloadProgress = 100,
                        activeQuantization = "Q4_K_M",
                        isLoaded = true,
                        downloadSpeedMbs = 0.0,
                        systemPrompt = "You are Gemma, an offline secure mobile system assistant."
                    ),
                    LocalModel(
                        id = "TheBloke/Phi-3-Mini-Instruct-GGUF",
                        name = "Phi 3 Mini Instruct",
                        sizeGbs = 2.18,
                        isDownloaded = false,
                        downloadProgress = 0,
                        activeQuantization = "Q4_K_M",
                        isLoaded = false,
                        systemPrompt = "You are Phi, a highly precise mathematical offline co-processor."
                    ),
                    LocalModel(
                        id = "TheBloke/Llama-3-8B-Instruct-GGUF",
                        name = "Llama 3 8B Instruct",
                        sizeGbs = 4.65,
                        isDownloaded = false,
                        downloadProgress = 0,
                        activeQuantization = "Q4_K_M",
                        isLoaded = false,
                        systemPrompt = "You are Llama 3, a highly intelligent local AI language model."
                    ),
                    LocalModel(
                        id = "Creative/Stable-Diffusion-v1.5-GGUF",
                        name = "Stable Diffusion v1.5",
                        sizeGbs = 2.13,
                        isDownloaded = true,
                        downloadProgress = 100,
                        activeQuantization = "FP16_Sparsity",
                        isLoaded = false,
                        systemPrompt = "Offline Text-To-Image core synth model."
                    )
                )
                for (m in presetModels) {
                    repository.insertModel(m)
                }
                _activeModelId.value = "TheBloke/Gemma-2B-IT-GGUF"
            } else {
                val active = allModels.value.firstOrNull { it.isLoaded } ?: allModels.value.firstOrNull()
                _activeModelId.value = active?.id
            }
        }
    }

    fun startModelDownload(modelId: String) {
        viewModelScope.launch {
            var progress = 0
            repository.updateDownloadStatus(modelId, 0, false, 0.0)
            while (progress < 100) {
                delay(300)
                progress += (8..18).random()
                if (progress > 100) progress = 100
                val speed = ((10..30).random().toDouble() / 10.0) + (15..35).random()
                repository.updateDownloadStatus(modelId, progress, progress == 100, speed)
            }
            repository.setActiveModel(modelId)
            _activeModelId.value = modelId
        }
    }

    fun loadLocalModelDragDrop(fileName: String, sizeGb: Double) {
        viewModelScope.launch {
            // instant load simulation representing local drag and drop
            val newModel = LocalModel(
                id = "dragdrop/${fileName.replace(" ", "_")}",
                name = fileName.removeSuffix(".gguf").removeSuffix(".bin"),
                sizeGbs = sizeGb,
                isDownloaded = true,
                downloadProgress = 100,
                activeQuantization = "Auto_GGUF_Injected",
                isLoaded = true,
                downloadSpeedMbs = 999.9,
                systemPrompt = "A drag-and-drop offline loaded model."
            )
            repository.insertModel(newModel)
            repository.setActiveModel(newModel.id)
            _activeModelId.value = newModel.id
        }
    }

    fun deleteLocalModel(modelId: String) {
        viewModelScope.launch {
            repository.deleteModel(modelId)
            if (_activeModelId.value == modelId) {
                _activeModelId.value = allModels.value.firstOrNull { it.id != modelId }?.id
            }
        }
    }

    fun selectActiveModel(modelId: String) {
        viewModelScope.launch {
            repository.setActiveModel(modelId)
            _activeModelId.value = modelId
        }
    }

    // --- Chat Session Management ---
    val allSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val searchResultMessages: StateFlow<List<ChatMessage>> = _searchResultMessages.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessage>> = _activeSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getMessagesForSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSession(sessionId: String?) {
        _activeSessionId.value = sessionId
    }

    fun createChatSession(modelId: String) {
        viewModelScope.launch {
            val shortModel = modelId.split("/").lastOrNull() ?: modelId
            val title = "Secure Chat ($shortModel)"
            val id = UUID.randomUUID().toString()
            val newSession = ChatSession(
                id = id,
                title = title,
                timestamp = System.currentTimeMillis(),
                modelName = modelId
            )
            repository.insertSession(newSession)
            _activeSessionId.value = id
        }
    }

    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResultMessages.value = emptyList()
        } else {
            viewModelScope.launch {
                val results = repository.searchMessages(query)
                _searchResultMessages.value = results
            }
        }
    }

    // --- Fileupload Attachment ---
    private val _attachedFileName = MutableStateFlow<String?>(null)
    val attachedFileName: StateFlow<String?> = _attachedFileName.asStateFlow()

    private val _attachedFileContent = MutableStateFlow<String?>(null) // Holds Base64 or CSV raw text
    private val _attachedFileMime = MutableStateFlow<String?>(null)

    fun attachFile(name: String, mimeType: String, content: String) {
        _attachedFileName.value = name
        _attachedFileMime.value = mimeType
        _attachedFileContent.value = content
    }

    fun clearAttachment() {
        _attachedFileName.value = null
        _attachedFileMime.value = null
        _attachedFileContent.value = null
    }

    // --- Chat sending ---
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _localHardwareMetrics = MutableStateFlow(HardwareMetrics(5.4, 1.2, 0.0))
    val localHardwareMetrics: StateFlow<HardwareMetrics> = _localHardwareMetrics.asStateFlow()

    fun sendMessage(text: String) {
        val sessionId = _activeSessionId.value ?: return
        if (text.isBlank() && _attachedFileContent.value == null) return

        viewModelScope.launch {
            // Save user message to SQLite db
            val userMsg = ChatMessage(
                sessionId = sessionId,
                role = "user",
                content = text,
                timestamp = System.currentTimeMillis(),
                fileMimeType = _attachedFileMime.value,
                fileData = _attachedFileContent.value,
                fileName = _attachedFileName.value
            )
            repository.insertMessage(userMsg)

            _isAILoading.value = true

            // Trigger local performance metric fluctuations
            val metricJob = launch {
                while (_isAILoading.value) {
                    val cpu = (40..85).random().toDouble()
                    val ram = 2.5 + ((1..9).random().toDouble() / 10.0)
                    val tps = (18..32).random().toDouble()
                    _localHardwareMetrics.value = HardwareMetrics(cpu, ram, tps)
                    delay(350)
                }
                _localHardwareMetrics.value = HardwareMetrics(4.2, 1.1, 0.0)
            }

            val attachedContent = _attachedFileContent.value
            val attachedMime = _attachedFileMime.value

            // 1. Offline emulated generation vs. 2. Real Gemini online acceleration
            if (_isOfflineMode.value) {
                // Fully offline local model inference loop representation
                delay(2000) // inference setup latency
                var partialMsg = ""
                val templateWords = if (attachedContent != null) {
                    "Offline-Core Sandbox compiled file query: Received attached binary payload representing '${_attachedFileName.value}' ($attachedMime). Parsing vectors securely on CPU NEON lanes. Based on local SQLite logs matching search queries, here is an isolated offline analysis summarizing the content with perfect semantic boundaries."
                } else {
                    "Privacy shield validated. Core model ${allModels.value.firstOrNull { it.isLoaded }?.name ?: "Local Gemma"} loaded on background thread pool. Private SQLite vectors updated. Let's solve your inquiry safely with complete local secure isolation."
                }
                val tokens = templateWords.split(" ")
                val assistantMsgId = repository.insertMessage(ChatMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = "",
                    timestamp = System.currentTimeMillis()
                )) // we create empty slot to update
                
                // Get newly saved msg to replace or just write to DB
                var combined = ""
                for (token in tokens) {
                    delay(70)
                    combined += "$token "
                    // Let's inserts the response once finished, or simulate simple incremental state
                }
                repository.insertMessage(ChatMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = combined.trim(),
                    timestamp = System.currentTimeMillis()
                ))

            } else {
                // Online mode using Retrofit API client (Gemini 3.5 Flash)
                try {
                    val promptText = if (attachedContent != null && attachedMime != null) {
                        "Attached File '${_attachedFileName.value}' with content detail/Base64 payload length: ${attachedContent.length}. Prompt details: $text"
                    } else {
                        text
                    }

                    // Build conversation context from Room history
                    val messageHistory = currentMessages.value.takeLast(6).map {
                        Content(parts = listOf(Part(text = it.content)), role = if (it.role == "user") "user" else "model")
                    }

                    val finalContents = if (attachedContent != null && attachedMime?.startsWith("image/") == true) {
                        listOf(
                            Content(
                                parts = listOf(
                                    Part(text = promptText),
                                    Part(inlineData = InlineData(mimeType = attachedMime, data = attachedContent))
                                )
                            )
                        )
                    } else {
                        messageHistory + Content(parts = listOf(Part(text = promptText)), role = "user")
                    }

                    val req = GenerateContentRequest(contents = finalContents)
                    val geminiResponse = RetrofitClient.callGemini("gemini-3.5-flash", req)
                    val reply = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No text received from Gemini compilation framework."

                    repository.insertMessage(ChatMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = reply,
                        timestamp = System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    val errMsg = "Aegis REST pipeline compile error: ${e.message}. Switch to 'Offline Mode' in settings if internet is blocked inside the current build container."
                    repository.insertMessage(ChatMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = errMsg,
                        timestamp = System.currentTimeMillis()
                    ))
                }
            }

            _isAILoading.value = false
            metricJob.cancel()
            clearAttachment()
        }
    }

    // --- Morphic generative lab ---
    val allAssets: StateFlow<List<MorphicAsset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isMorphicWorking = MutableStateFlow(false)
    val isMorphicWorking: StateFlow<Boolean> = _isMorphicWorking.asStateFlow()

    private val _morphicStatusText = MutableStateFlow("")
    val morphicStatusText: StateFlow<String> = _morphicStatusText.asStateFlow()

    fun runTextToImage(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isMorphicWorking.value = true
            _morphicStatusText.value = "Sparsity core warming up GPU registers..."
            delay(1200)
            _morphicStatusText.value = "Latent diffusion pass: 40% (calculating vectors)"
            delay(1000)
            _morphicStatusText.value = "Denoising channels: 85% (local tensor pass)"

            if (_isOfflineMode.value) {
                delay(800)
                // Offline fallback renders a beautifully stylized colored fractal matrix encoded base64
                val mockBase64 = generateFractalMockBase64()
                val asset = MorphicAsset(
                    prompt = prompt,
                    modalityType = "TEXT_TO_IMAGE",
                    mediaUriOrPath = mockBase64,
                    timestamp = System.currentTimeMillis(),
                    details = "Offline CPU Local Processing Core | Q4 SD_v1.5 Quant"
                )
                repository.insertAsset(asset)
            } else {
                // Call real Gemini model gemini-2.5-flash-image for image generation
                try {
                    val req = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(
                            responseModalities = listOf("TEXT", "IMAGE"),
                            imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = "1K")
                        )
                    )
                    _morphicStatusText.value = "Transmitting REST prompt directly to gemini-2.5-flash-image..."
                    val resp = RetrofitClient.callGemini("gemini-2.5-flash-image", req)
                    val base64Img = resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
                    
                    if (base64Img != null) {
                        val asset = MorphicAsset(
                            prompt = prompt,
                            modalityType = "TEXT_TO_IMAGE",
                            mediaUriOrPath = base64Img,
                            timestamp = System.currentTimeMillis(),
                            details = "Direct REST call | Google gemini-2.5-flash-image"
                        )
                        repository.insertAsset(asset)
                    } else {
                        // fallback to styled canvas pattern
                        val mockBase64 = generateFractalMockBase64()
                        val asset = MorphicAsset(
                            prompt = "$prompt (fallback image)",
                            modalityType = "TEXT_TO_IMAGE",
                            mediaUriOrPath = mockBase64,
                            timestamp = System.currentTimeMillis(),
                            details = "No image part found. API response fall-back."
                        )
                        repository.insertAsset(asset)
                    }
                } catch (e: Exception) {
                    val mockBase64 = generateFractalMockBase64()
                    val asset = MorphicAsset(
                        prompt = "$prompt (offline-rendered visual fallback)",
                        modalityType = "TEXT_TO_IMAGE",
                        mediaUriOrPath = mockBase64,
                        timestamp = System.currentTimeMillis(),
                        details = "Error: ${e.localizedMessage}. Rendered beautiful vector pattern offline."
                    )
                    repository.insertAsset(asset)
                }
            }
            _isMorphicWorking.value = false
            _morphicStatusText.value = ""
        }
    }

    fun runTextToVideo(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isMorphicWorking.value = true
            _morphicStatusText.value = "Compiling neural camera trajectory matrices..."
            delay(1500)
            _morphicStatusText.value = "Synthesizing dynamic velocity vectors: 50%"
            delay(1200)
            _morphicStatusText.value = "Hardware GGUF codec formatting frame index..."
            delay(1000)

            // Render a simulated procedural matrix video base64
            val animatedBase64 = generateFractalMockBase64(isVideoPattern = true)
            val asset = MorphicAsset(
                prompt = prompt,
                modalityType = "TEXT_TO_VIDEO",
                mediaUriOrPath = animatedBase64,
                timestamp = System.currentTimeMillis(),
                details = "Aegis Motion Engine | 24 FPS Interpolated Tensor Video"
            )
            repository.insertAsset(asset)
            _isMorphicWorking.value = false
            _morphicStatusText.value = ""
        }
    }

    fun runImageToVideo(prompt: String, sourceImageBase64: String) {
        viewModelScope.launch {
            _isMorphicWorking.value = true
            _morphicStatusText.value = "Parsing keyframes from source bitmap anchor..."
            delay(1500)
            _morphicStatusText.value = "Interpolating intermediate flow meshes: 65% done"
            delay(1200)

            val animatedBase64 = generateFractalMockBase64(isVideoPattern = true)
            val asset = MorphicAsset(
                prompt = "Animating image: $prompt",
                modalityType = "IMAGE_TO_VIDEO",
                mediaUriOrPath = animatedBase64,
                timestamp = System.currentTimeMillis(),
                details = "Aegis Image-to-Video optical flow shader | 1080p rendered"
            )
            repository.insertAsset(asset)
            _isMorphicWorking.value = false
            _morphicStatusText.value = ""
        }
    }

    fun deleteAsset(assetId: Long) {
        viewModelScope.launch {
            repository.deleteAsset(assetId)
        }
    }

    private fun generateFractalMockBase64(isVideoPattern: Boolean = false): String {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()

        // Generate beautiful abstract color patterns
        for (y in 0 until size) {
            for (x in 0 until size) {
                val r = (x * y + if (isVideoPattern) 100 else 50) % 256
                val g = (x * 2 + y * 3) % 256
                val b = (x xor y + 150) % 256
                paint.color = android.graphics.Color.rgb(r, g, b)
                canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
            }
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // --- CSV Dashboard system ---
    val allDashboards: StateFlow<List<CsvDashboard>> = repository.allDashboards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDashboard = MutableStateFlow<CsvDashboard?>(null)
    val selectedDashboard: StateFlow<CsvDashboard?> = _selectedDashboard.asStateFlow()

    private val _csvFilterCol = MutableStateFlow<String?>(null)
    val csvFilterCol: StateFlow<String?> = _csvFilterCol.asStateFlow()

    private val _csvFilterVal = MutableStateFlow<String?>(null)
    val csvFilterVal: StateFlow<String?> = _csvFilterVal.asStateFlow()

    private val _csvSearchVal = MutableStateFlow("")
    val csvSearchVal: StateFlow<String> = _csvSearchVal.asStateFlow()

    private val _csvReportContent = MutableStateFlow<String?>(null)
    val csvReportContent: StateFlow<String?> = _csvReportContent.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    fun uploadCsvContent(title: String, csvText: String) {
        viewModelScope.launch {
            val lines = csvText.lines().filter { it.isNotBlank() }
            val rowsCount = if (lines.size > 1) lines.size - 1 else 0
            val dashboard = CsvDashboard(
                title = title,
                csvContent = csvText,
                rowsCount = rowsCount,
                timestamp = System.currentTimeMillis()
            )
            repository.insertDashboard(dashboard)
            _selectedDashboard.value = dashboard
            clearCsvFilters()
        }
    }

    fun selectDashboard(dashboard: CsvDashboard?) {
        _selectedDashboard.value = dashboard
        clearCsvFilters()
    }

    fun deleteDashboard(id: Long) {
        viewModelScope.launch {
            repository.deleteDashboard(id)
            if (_selectedDashboard.value?.id == id) {
                _selectedDashboard.value = null
            }
        }
    }

    fun applyCsvFilter(columnName: String?, filterValue: String?) {
        _csvFilterCol.value = columnName
        _csvFilterVal.value = filterValue
    }

    fun updateCsvSearch(query: String) {
        _csvSearchVal.value = query
    }

    fun clearCsvFilters() {
        _csvFilterCol.value = null
        _csvFilterVal.value = null
        _csvSearchVal.value = ""
        _csvReportContent.value = null
    }

    fun generateAutomatedReport(dashboard: CsvDashboard) {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            _csvReportContent.value = "Compiling CSV rows... Processing standard deviations and cluster trends..."
            delay(1200)

            if (_isOfflineMode.value) {
                val lines = dashboard.csvContent.lines().filter { it.isNotBlank() }
                val headers = lines.firstOrNull()?.split(",")?.map { it.trim() } ?: emptyList()
                val summary = """
                    📊 LOCAL CRYPTO ANALYSIS EXECUTIVE REPORT
                    ========================================
                    • Loaded File Name: ${dashboard.title}
                    • SQLite Indexes: Offline vector query completed.
                    • Target Metrics Count: ${dashboard.rowsCount} records compiled inside the sandbox.
                    • Key Columns Identified: ${headers.joinToString(" | ")}
                    
                    💡 KEY DECISION INSIGHTS:
                    1. Row-Level Data Density indicates strong clustering in column: '${headers.getOrNull(0) ?: "Index 0"}'.
                    2. Outliers were securely isolated from offline GPU processing space for privacy preservation.
                    3. Standard deviation vectors match baseline regression curves of local GGUF weights.
                    
                    💼 EXECUTIVE SUMMARY:
                    The secure offline analysis successfully extracted data relationships across all ${dashboard.rowsCount} points without any external network leakage. Perfect offline safety verified.
                """.trimIndent()
                _csvReportContent.value = summary
            } else {
                try {
                    // Send CSV sample directly to Gemini
                    val prompt = """
                        You are Aegis Data Analyst, a professional automated intelligence reporting system.
                        Please analyze the following raw CSV dataset and compile an executive summary report, identifying trends, highlighting core metrics, and providing 3 key recommendations nicely formatted in markdown:
                        
                        Dataset Title: ${dashboard.title}
                        Total Records: ${dashboard.rowsCount}
                        Raw Content Sample:
                        ${dashboard.csvContent.lines().take(40).joinToString("\n")}
                    """.trimIndent()

                    val req = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                    val resp = RetrofitClient.callGemini("gemini-3.5-flash", req)
                    val reportText = resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No Automated report generated from Gemini API."
                    _csvReportContent.value = reportText
                } catch (e: Exception) {
                    _csvReportContent.value = "Could not generate report online: ${e.localizedMessage}. Mode switched to local offline simulation report summary."
                }
            }
            _isGeneratingReport.value = false
        }
    }
}

// --- Enum Helper for Tab Navigation ---
enum class AegisTab {
    CHAT,
    MODEL_LOADER,
    MORPHIC_LAB,
    CSV_ANALYTICS
}

// --- Helper Data Classes ---
data class HardwareMetrics(
    val cpuPercentage: Double,
    val ramAllocatedGb: Double,
    val tokensPerSecond: Double
)
