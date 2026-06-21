package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AegisTab
import com.example.MainViewModel
import com.example.R
import com.example.data.ChatMessage
import com.example.data.LocalModel
import com.example.data.MorphicAsset
import com.example.data.CsvDashboard
import java.io.ByteArrayOutputStream

// Custom Cyber Palette -> Sophisticated Dark Theme Palette
val CyanPrimary = Color(0xFFD0BCFF) // Elegant primary purple/lavender
val CyanGlow = Color(0x22D0BCFF) // Soft lavender glow
val CharcoalDark = Color(0xFF121212) // Pure dark background
val CharcoalSlate = Color(0xFF1C1B1F) // Deep obsidian surface/header
val CharcoalSurface = Color(0xFF2B2930) // Dark violet-grey cards
val AlertGreen = Color(0xFF81C784) // Safe offline success green
val MutedSlate = Color(0xFF938F99) // Soft mid-grey subtitle
val SophisticatedOutline = Color(0xFF49454F) // Muted container borders
val DeepPurpleContainer = Color(0xFF381E72) // Distinct dark purple accent container
val HardwareBarBg = Color(0xFF25232A) // Medium hardware telemetry bar background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AegisLocalAiApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val hardwareMetrics by viewModel.localHardwareMetrics.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = CharcoalDark,
        topBar = {
            AegisTopAppBar(
                isOfflineMode = isOfflineMode,
                onToggleOffline = { viewModel.toggleOfflineMode() },
                cpu = hardwareMetrics.cpuPercentage,
                ram = hardwareMetrics.ramAllocatedGb,
                tps = hardwareMetrics.tokensPerSecond
            )
        },
        bottomBar = {
            AegisBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(fadeInSpec()) togetherWith fadeOut(fadeOutSpec())
                },
                label = "MainTabTransition"
            ) { tab ->
                when (tab) {
                    AegisTab.CHAT -> ChatHubScreen(viewModel)
                    AegisTab.MODEL_LOADER -> ModelLoaderScreen(viewModel)
                    AegisTab.SPOKEN_PRACTICE -> SpokenPracticeScreen(viewModel)
                    AegisTab.MORPHIC_LAB -> MorphicLabScreen(viewModel)
                    AegisTab.CSV_ANALYTICS -> CsvAnalyticsScreen(viewModel)
                }
            }
        }
    }
}

// Animation Specs
fun <T> fadeInSpec() = androidx.compose.animation.core.tween<T>(durationMillis = 200)
fun <T> fadeOutSpec() = androidx.compose.animation.core.tween<T>(durationMillis = 150)

// --- Custom Cyber Top Bar ---
@Composable
fun AegisTopAppBar(
    isOfflineMode: Boolean,
    onToggleOffline: () -> Unit,
    cpu: Double,
    ram: Double,
    tps: Double
) {
    Surface(
        color = CharcoalSlate,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, SophisticatedOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title and Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(CyanPrimary, DeepPurpleContainer))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Logo",
                            tint = CharcoalSlate,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AEGIS SECURE-CORE",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isOfflineMode) AlertGreen else CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOfflineMode) "OFFLINE SANDBOX ISOLATION" else "CLOUD-ACCELERATED API",
                                color = if (isOfflineMode) AlertGreen else CyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Toggle and Metrics
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AIR-GAP",
                        color = MutedSlate,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { onToggleOffline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AlertGreen,
                            checkedTrackColor = AlertGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = CyanPrimary,
                            uncheckedTrackColor = CyanPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hardware Telemetry Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(HardwareBarBg)
                    .border(BorderStroke(1.dp, SophisticatedOutline), RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryStat("CPU_CORES", String.format("%.1f%%", cpu), isOfflineMode)
                TelemetryStat("RAM_ALLOC", String.format("%.2f GB", ram), isOfflineMode)
                TelemetryStat("INF_TPS", if (tps > 0) String.format("%.1f T/s", tps) else "STANDBY", isOfflineMode)
            }
        }
    }
}

@Composable
fun TelemetryStat(label: String, value: String, highlight: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            color = MutedSlate,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = if (label == "INF_TPS" && value != "STANDBY") AlertGreen else if (highlight) AlertGreen else CyanPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// Extension to scale elements easily
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            (placeable.width * scale).toInt(),
            (placeable.height * scale).toInt()
        ) {
            placeable.placeWithLayer(0, 0)
        }
    }
)


// --- Custom Cyber Bottom Bar ---
@Composable
fun AegisBottomNavigationBar(
    currentTab: AegisTab,
    onTabSelected: (AegisTab) -> Unit
) {
    NavigationBar(
        containerColor = CharcoalSlate,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, SophisticatedOutline))
    ) {
        NavigationBarItem(
            selected = currentTab == AegisTab.CHAT,
            onClick = { onTabSelected(AegisTab.CHAT) },
            icon = { Icon(Icons.Default.Share, contentDescription = "Secure Chat") },
            label = { Text("Chat Hub", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                indicatorColor = DeepPurpleContainer,
                unselectedIconColor = MutedSlate,
                unselectedTextColor = MutedSlate
            )
        )
        NavigationBarItem(
            selected = currentTab == AegisTab.MODEL_LOADER,
            onClick = { onTabSelected(AegisTab.MODEL_LOADER) },
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Model Manager") },
            label = { Text("Model Hub", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                indicatorColor = DeepPurpleContainer,
                unselectedIconColor = MutedSlate,
                unselectedTextColor = MutedSlate
            )
        )
        NavigationBarItem(
            selected = currentTab == AegisTab.SPOKEN_PRACTICE,
            onClick = { onTabSelected(AegisTab.SPOKEN_PRACTICE) },
            icon = { Icon(Icons.Default.Done, contentDescription = "English Spoken Practice") },
            label = { Text("Speak Hub", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                indicatorColor = DeepPurpleContainer,
                unselectedIconColor = MutedSlate,
                unselectedTextColor = MutedSlate
            )
        )
        NavigationBarItem(
            selected = currentTab == AegisTab.MORPHIC_LAB,
            onClick = { onTabSelected(AegisTab.MORPHIC_LAB) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Generative Multi-morphic lab") },
            label = { Text("Morphic", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                indicatorColor = DeepPurpleContainer,
                unselectedIconColor = MutedSlate,
                unselectedTextColor = MutedSlate
            )
        )
        NavigationBarItem(
            selected = currentTab == AegisTab.CSV_ANALYTICS,
            onClick = { onTabSelected(AegisTab.CSV_ANALYTICS) },
            icon = { Icon(Icons.Default.List, contentDescription = "CSV to Dashboard") },
            label = { Text("Analytics", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanPrimary,
                selectedTextColor = CyanPrimary,
                indicatorColor = DeepPurpleContainer,
                unselectedIconColor = MutedSlate,
                unselectedTextColor = MutedSlate
            )
        )
    }
}

// --- SCREEN 1: Secure Chat Screen ---
@Composable
fun ChatHubScreen(viewModel: MainViewModel) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAILoading.collectAsStateWithLifecycle()
    val models by viewModel.allModels.collectAsStateWithLifecycle()
    val activeModelId by viewModel.activeModelId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResultMessages.collectAsStateWithLifecycle()
    val attachedFileName by viewModel.attachedFileName.collectAsStateWithLifecycle()

    var showFileDrawer by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // auto scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (activeSessionId == null) {
        // Chat Dashboard Home
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Visual Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalSlate)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aegis_banner),
                    contentDescription = "Aegis Abstract Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CharcoalDark),
                                startY = 50f
                            )
                        )
                )
                Text(
                    text = "Aegis Private Workspace",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search History Panel
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search SQL conversation history...", color = MutedSlate, fontFamily = FontFamily.Monospace) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = CyanPrimary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = SophisticatedOutline,
                    focusedContainerColor = CharcoalSlate,
                    unfocusedContainerColor = CharcoalSlate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isNotEmpty()) {
                // Display searchable SQLite indexes
                Text(
                    text = "SQLite INDEX RESULTS (${searchResults.size})",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (searchResults.isEmpty()) {
                    Text("No records matched.", color = MutedSlate, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                } else {
                    searchResults.forEach { msg ->
                        Card(
                            onClick = { viewModel.selectSession(msg.sessionId) },
                            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (msg.role == "user") "USER QUERY" else "SECURE CO-PROCESSOR",
                                        color = if (msg.role == "user") MutedSlate else AlertGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "In Session: ${msg.sessionId.take(6)}...",
                                        color = MutedSlate,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.content,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                // Active Sessions List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE SECURE SESSIONS (${sessions.size})",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = {
                            val targetModel = activeModelId ?: models.firstOrNull()?.id ?: "Gemma-2B"
                            viewModel.createChatSession(targetModel)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add chat", tint = CharcoalDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SPAWN NEW", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, SophisticatedOutline), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No private session buffers loaded.\nSpawn a secure chamber above to begin.",
                            color = MutedSlate,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    sessions.forEach { sess ->
                        val shortModel = sess.modelName.split("/").lastOrNull() ?: sess.modelName
                        Card(
                            onClick = { viewModel.selectSession(sess.id) },
                            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = BorderStroke(1.dp, SophisticatedOutline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, contentDescription = "Session icon", tint = CyanPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(sess.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Isolated slot: $shortModel", color = MutedSlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteChatSession(sess.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Chat Active Room
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Info
            Surface(
                color = CharcoalSlate,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SophisticatedOutline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.selectSession(null) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Back", tint = CyanPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val activeSession = sessions.firstOrNull { it.id == activeSessionId }
                            Text(
                                text = activeSession?.title ?: "Secure Chamber",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "AIR-GAP BUFFER LOCKED",
                                color = AlertGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.deleteChatSession(activeSessionId!!) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Chamber", tint = Color(0xFFFF5252))
                    }
                }
            }

            // Message Scrolling Window
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "STREAMS SECURELY ENCRYPTED\nSend a request to activate model vectors.",
                                color = MutedSlate,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                }
                if (isAiLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CyanPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Local GPU/CPU inference processing GGUF weights...",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // File Attachment Drawer
            if (showFileDrawer) {
                Surface(
                    color = CharcoalSlate,
                    border = BorderStroke(1.dp, SophisticatedOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SECURE SANDBOX FILE INJECTOR (LOCAL MOCK)",
                            color = CyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachmentShortcut("Financials.csv", "text/csv") {
                                viewModel.attachFile("Financials.csv", "text/csv", "Report,Value,Date\nMargin,85%,2026\nRevenue,4M,2026\nAssetRatio,1.8,2026")
                                showFileDrawer = false
                            }
                            AttachmentShortcut("System_Log.txt", "text/plain") {
                                viewModel.attachFile("System_Log.txt", "text/plain", "KERN: Aegis kernel boot sequence. CPU Temp 42C. Memory isolated.")
                                showFileDrawer = false
                            }
                            AttachmentShortcut("MockAsset.jpg", "image/jpeg") {
                                // Simple mock Base64 representation of a JPEG
                                viewModel.attachFile("MockAsset.jpg", "image/jpeg", "/9j/4AAQSkZJRgABAQ")
                                showFileDrawer = false
                            }
                        }
                    }
                }
            }

            // Chat Input Row
            Surface(
                color = CharcoalSlate,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(BorderStroke(1.dp, SophisticatedOutline))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showFileDrawer = !showFileDrawer }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach file",
                            tint = if (attachedFileName != null) AlertGreen else CyanPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        if (attachedFileName != null) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1B2E24))
                                    .padding(vertical = 2.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Attached", tint = AlertGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = attachedFileName!!,
                                    color = AlertGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { viewModel.clearAttachment() },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(10.dp))
                                }
                            }
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Compile queries completely offline...", color = MutedSlate, fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() || attachedFileName != null) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = !isAiLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isAiLoading) CyanPrimary else MutedSlate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentShortcut(name: String, mime: String, onSelect: () -> Unit) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(containerColor = CharcoalDark),
        border = BorderStroke(1.dp, SophisticatedOutline),
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (mime.startsWith("image/")) Icons.Default.PlayArrow else Icons.Default.List,
                contentDescription = "Attachment shortcut",
                tint = CyanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 12.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) CharcoalSlate else CharcoalSurface
                ),
                border = BorderStroke(
                    1.dp,
                    SophisticatedOutline
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isUser) "LOCAL OWNER" else "ISOLATED PIPELINE RESPONSE",
                            color = if (isUser) CyanPrimary else AlertGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        val date = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(msg.timestamp)
                        Text(
                            text = date,
                            color = MutedSlate,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (msg.fileName != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CharcoalDark)
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "File attached logo", tint = CyanPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Loaded binary block: ${msg.fileName}",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg.content,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- SCREEN 2: Model Loader / Download Hub ---
@Composable
fun ModelLoaderScreen(viewModel: MainViewModel) {
    val models by viewModel.allModels.collectAsStateWithLifecycle()
    val activeId by viewModel.activeModelId.collectAsStateWithLifecycle()

    var customRepoInput by remember { mutableStateOf("") }
    var customSizeInput by remember { mutableStateOf("2.5") }
    var draggingAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "HUGGING FACE MODEL MANAGER",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Drag & Drop Emulation Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.sweepGradient(listOf(CyanPrimary, Color.Transparent, CyanPrimary))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(CharcoalSlate)
                .clickable {
                    draggingAlert = !draggingAlert
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Upload model file",
                    tint = CyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DRAG & DROP GGUF INTO COMPILATION BLOCK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to load verified model binary from storage instantly",
                    color = MutedSlate,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (draggingAlert) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = CharcoalSurface,
                border = BorderStroke(1.dp, CyanPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Secure Inject Sandbox GGUF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customRepoInput,
                        onValueChange = { customRepoInput = it },
                        placeholder = { Text("Llama-3-Instruct-8B.gguf", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val name = if (customRepoInput.isBlank()) "Custom_Model.gguf" else customRepoInput
                                viewModel.loadLocalModelDragDrop(name, customSizeInput.toDoubleOrNull() ?: 2.5)
                                draggingAlert = false
                                customRepoInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("INJECT IMMEDIATELY", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "HUGGING FACE DOWNLOAD CHAMBER",
            color = MutedSlate,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Model items list
        models.forEach { model ->
            val isActive = activeId == model.id
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                border = BorderStroke(1.dp, if (isActive) CyanPrimary else SophisticatedOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(model.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(model.id, color = MutedSlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        // Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isActive) AlertGreen.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.2f))
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (isActive) "ACTIVE WEIGHTS" else if (model.isDownloaded) "DOWNLOADED" else "HF REMOTE",
                                color = if (isActive) AlertGreen else CyanPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Size: ${model.sizeGbs} GB  |  Quant: ${model.activeQuantization}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (!model.isDownloaded && model.downloadProgress == 0) {
                            Button(
                                onClick = { viewModel.startModelDownload(model.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("SECURE CLONE", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        } else if (model.downloadProgress in 1..99) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Speed: ${String.format("%.1f MB/s", model.downloadSpeedMbs)}",
                                    color = CyanPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            if (!isActive) {
                                Button(
                                    onClick = { viewModel.selectActiveModel(model.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                    border = BorderStroke(1.dp, CyanPrimary),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("LOAD IN MEMORY", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Progress slider display if downloading
                    if (model.downloadProgress in 1..99) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = model.downloadProgress.toFloat() / 100f,
                                color = CyanPrimary,
                                trackColor = CharcoalDark,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${model.downloadProgress}%",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: Morphic Generative Multimedia Lab ---
@Composable
fun MorphicLabScreen(viewModel: MainViewModel) {
    val assets by viewModel.allAssets.collectAsStateWithLifecycle()
    val working by viewModel.isMorphicWorking.collectAsStateWithLifecycle()
    val statusText by viewModel.morphicStatusText.collectAsStateWithLifecycle()

    var generationPrompt by remember { mutableStateOf("") }
    var selectedModality by remember { mutableStateOf("TEXT_TO_IMAGE") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "AEGIS MORPHIC LAB (SECURE LOCAL GPU)",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Modality Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalSlate)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ModalityPill("Text2Img", selectedModality == "TEXT_TO_IMAGE") { selectedModality = "TEXT_TO_IMAGE" }
            ModalityPill("Text2Vid", selectedModality == "TEXT_TO_VIDEO") { selectedModality = "TEXT_TO_VIDEO" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt input
        OutlinedTextField(
            value = generationPrompt,
            onValueChange = { generationPrompt = it },
            placeholder = { Text("Describe dynamic assets to render...", color = MutedSlate, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = SophisticatedOutline,
                focusedContainerColor = CharcoalSlate,
                unfocusedContainerColor = CharcoalSlate
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (working) {
            Surface(
                color = CharcoalSlate,
                border = BorderStroke(1.dp, CyanPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusText, color = CyanPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                }
            }
        } else {
            Button(
                onClick = {
                    if (selectedModality == "TEXT_TO_IMAGE") {
                        viewModel.runTextToImage(generationPrompt)
                    } else {
                        viewModel.runTextToVideo(generationPrompt)
                    }
                    generationPrompt = ""
                },
                enabled = generationPrompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SPARK SECURE GENERATION",
                    color = CharcoalDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MORPHIC RENDERING VAULT (${assets.size})",
            color = MutedSlate,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (assets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, SophisticatedOutline), RoundedCornerShape(8.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Render vault is empty.\nSynthesize an asset above.", color = MutedSlate, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, fontSize = 13.sp)
            }
        } else {
            assets.forEach { asset ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                    border = BorderStroke(1.dp, SophisticatedOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column {
                        // Display decoded base64 image or fractal vector beautiful pattern
                        val bitmap = remember(asset.mediaUriOrPath) {
                            try {
                                val bytes = Base64.decode(asset.mediaUriOrPath, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Asset preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Parsing render frame...", color = Color.White)
                            }
                        }

                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = asset.modalityType,
                                    color = CyanPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(asset.timestamp),
                                    color = MutedSlate,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(asset.prompt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(asset.details ?: "Secure Core Synth", color = MutedSlate, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            IconButton(onClick = { viewModel.deleteAsset(asset.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete asset", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModalityPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CyanPrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 16.dp)
    ) {
        Text(
            label,
            color = if (selected) CharcoalDark else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// --- SCREEN 4: CSV to Dashboard Analytics Screen ---
@Composable
fun CsvAnalyticsScreen(viewModel: MainViewModel) {
    val dashboards by viewModel.allDashboards.collectAsStateWithLifecycle()
    val selected by viewModel.selectedDashboard.collectAsStateWithLifecycle()
    val filterCol by viewModel.csvFilterCol.collectAsStateWithLifecycle()
    val filterVal by viewModel.csvFilterVal.collectAsStateWithLifecycle()
    val searchVal by viewModel.csvSearchVal.collectAsStateWithLifecycle()
    val reportContent by viewModel.csvReportContent.collectAsStateWithLifecycle()
    val reportLoading by viewModel.isGeneratingReport.collectAsStateWithLifecycle()

    var csvTitleInput by remember { mutableStateOf("") }
    var csvRawInput by remember { mutableStateOf("") }
    var openCreator by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "CSV INTERACTIVE SECURE DASHBOARDS",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (selected == null) {
            // Dashboard Selector & Past Uploads
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHOOSE SECURE CSV SHEET",
                    color = MutedSlate,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = { openCreator = !openCreator },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add CSV", tint = CharcoalDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IMPORT NEW", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            if (openCreator) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = CharcoalSlate,
                    border = BorderStroke(1.dp, CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Create Interactive Sandbox Sheet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = csvTitleInput,
                            onValueChange = { csvTitleInput = it },
                            placeholder = { Text("Cryptocurrency_Yield_Metrics", color = MutedSlate) },
                            label = { Text("Sheet Title", color = CyanPrimary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = csvRawInput,
                            onValueChange = { csvRawInput = it },
                            placeholder = { Text("Asset,Yield%,MarketCap\nBitcoin,12.5%,1120B\nEthereum,8.2%,380B\nGemma-Vector,150.1%,12B\nSparsity,45.2%,4B", color = MutedSlate) },
                            label = { Text("Raw CSV Content", color = CyanPrimary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 10
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            // Quick fill preset button
                            TextButton(onClick = {
                                csvTitleInput = "Alpha_Crypto_Vect"
                                csvRawInput = "Asset,ReturnPercent,RiskFactor,IsolationSpeed\nAlphaNode,145.2,High,85ms\nBetaSpars,92.5,Medium,120ms\nGemmaGGUF,210.8,Low,12ms\nLlamaProc,118.4,Low,34ms\nStableDiff,45.2,Medium,220ms\nVeVideo,189.6,High,620ms"
                            }) {
                                Text("PRESET DEMO CSV", color = CyanPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Button(
                                onClick = {
                                    val title = if (csvTitleInput.isBlank()) "Unnamed Sheet" else csvTitleInput
                                    val content = if (csvRawInput.isBlank()) "Header1,Header2\nValue1,Value2" else csvRawInput
                                    viewModel.uploadCsvContent(title, content)
                                    openCreator = false
                                    csvTitleInput = ""
                                    csvRawInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Text("COMPILE SHEET", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (dashboards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, SophisticatedOutline), RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local secure CSV boards compiled.\nPaste or load one above.", color = MutedSlate, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            } else {
                dashboards.forEach { dash ->
                    Card(
                        onClick = { viewModel.selectDashboard(dash) },
                        colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dash.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Compiled records: ${dash.rowsCount}", color = CyanPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row {
                                IconButton(onClick = { viewModel.deleteDashboard(dash.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete board", tint = Color(0xFFFF5252))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive Active Sheet Dashboard
            val currentDashboard = selected!!
            val lines = currentDashboard.csvContent.lines().filter { it.isNotBlank() }
            val headers = lines.firstOrNull()?.split(",")?.map { it.trim() } ?: emptyList()
            var rows = lines.drop(1).map { it.split(",").map { cell -> cell.trim() } }

            // Apply searching
            if (searchVal.isNotBlank()) {
                rows = rows.filter { row ->
                    row.any { it.contains(searchVal, ignoreCase = true) }
                }
            }

            // Apply interactive column filtering
            if (filterCol != null && filterVal != null) {
                val colIdx = headers.indexOf(filterCol)
                if (colIdx != -1) {
                    rows = rows.filter { row -> row.getOrNull(colIdx) == filterVal }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectDashboard(null) }) {
                    Icon(Icons.Default.Share, contentDescription = "Back", tint = CyanPrimary)
                }
                Text(
                    text = currentDashboard.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = { viewModel.clearCsvFilters() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset filters", tint = CyanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search within compiled sheet
            OutlinedTextField(
                value = searchVal,
                onValueChange = { viewModel.updateCsvSearch(it) },
                placeholder = { Text("Filter cells dynamically in offline SQL...", color = MutedSlate, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Color(0xFF232B3C)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trend Visualizer: Canvas Drawn Bar Chart!
            Text(
                text = "REAL-TIME RENDERING VECTOR BAR GRAPH",
                color = MutedSlate,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CharcoalSlate)
                    .border(BorderStroke(1.dp, Color(0xFF232B3C)))
                    .padding(16.dp)
            ) {
                // Collect values from the 2nd column (often return or value metric)
                val numericValues = rows.mapNotNull { row ->
                    row.getOrNull(1)?.toDoubleOrNull()
                }
                val labelItems = rows.map { it.firstOrNull() ?: "" }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = numericValues.maxOrNull() ?: 1.0
                    val widthScale = size.width / rows.size.coerceAtLeast(1)
                    val bottomY = size.height

                    rows.forEachIndexed { idx, row ->
                        val numericVal = row.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        val barHeight = (numericVal / maxVal) * (bottomY - 20)
                        val barWidth = widthScale * 0.7f
                        val startX = idx * widthScale + (widthScale * 0.15f)
                        val startY = bottomY - barHeight.toFloat()

                        // Check if item is selected with filter
                        val isHighlighted = filterCol == headers.firstOrNull() && filterVal == row.firstOrNull()

                        drawRect(
                            color = if (isHighlighted) AlertGreen else CyanPrimary,
                            topLeft = androidx.compose.ui.geometry.Offset(startX, startY),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.toFloat())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Row cells with Filter click vectors
            Text(
                text = "INTERACTIVE CELL ROWS (FILTER ON CLICK)",
                color = MutedSlate,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Headers row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface)
                    .padding(6.dp)
            ) {
                headers.forEach { h ->
                    Text(
                        text = h.uppercase(),
                        color = CyanPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            // Row items list
            rows.take(15).forEach { row ->
                val keyword = row.firstOrNull()
                val isHighlighted = filterCol == headers.firstOrNull() && filterVal == keyword

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isHighlighted) Color(0x2200E676) else Color.Transparent)
                        .clickable {
                            if (isHighlighted) {
                                viewModel.applyCsvFilter(null, null)
                            } else {
                                if (keyword != null && headers.isNotEmpty()) {
                                    viewModel.applyCsvFilter(headers[0], keyword)
                                }
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .border(
                            BorderStroke(
                                0.5.dp,
                                if (isHighlighted) AlertGreen else Color(0x11FFFFFF)
                            )
                        )
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Executive Automated Report generator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTOMATED INTELLIGENCE REPORT",
                    color = CyanPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (!reportLoading) {
                    Button(
                        onClick = { viewModel.generateAutomatedReport(currentDashboard) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("COMPILE REPORT", color = CharcoalDark, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (reportLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalSlate)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AegisProgressIndicator(color = CyanPrimary, size = 20.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Synthesizing metrics data patterns offline...", color = MutedSlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            } else if (reportContent != null) {
                Surface(
                    color = CharcoalSlate,
                    border = BorderStroke(1.dp, AlertGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECURE EXECUTIVE DIAGNOSTICS",
                            color = AlertGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = reportContent!!,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// Simple CircularProgressIndicator modifier help
@Composable
fun AegisProgressIndicator(color: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 24.dp) {
    CircularProgressIndicator(
        color = color,
        modifier = modifier.size(size),
        strokeWidth = 3.dp
    )
}

// --- English Accent Spoken Practice Screen ---
@Composable
fun SpokenPracticeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val allModels by viewModel.allModels.collectAsStateWithLifecycle()
    val isEvaluating by viewModel.isSpokenEvaluating.collectAsStateWithLifecycle()
    val spokenLanguage by viewModel.spokenLanguage.collectAsStateWithLifecycle()
    
    val score by viewModel.spokenScore.collectAsStateWithLifecycle()
    val transcription by viewModel.spokenTranscription.collectAsStateWithLifecycle()
    val accentTips by viewModel.spokenFeedbackAccentTips.collectAsStateWithLifecycle()
    val mtiTraits by viewModel.spokenFeedbackMtiTraits.collectAsStateWithLifecycle()
    val nativeHelp by viewModel.spokenFeedbackNativeHelp.collectAsStateWithLifecycle()

    // Conversational Spoken States
    val currentMode by viewModel.spokenModeState.collectAsStateWithLifecycle()
    val conversation by viewModel.spokenConversationList.collectAsStateWithLifecycle()
    val isConvEvaluating by viewModel.isConvEvaluating.collectAsStateWithLifecycle()

    val targetModelId = "TheBloke/Llama-3-SpeechAccentPractice-GGUF"
    val speechModel = allModels.find { it.id == targetModelId }
    val isModelDownloaded = speechModel?.isDownloaded == true

    var activePhraseIndex by remember { mutableStateOf(0) }
    val challengingPhrases = listOf(
        "Can I get a bottle of water, please?",
        "My workday schedule is extremely comfortable.",
        "I am an offline Android software developer.",
        "Look at that beautiful sunset across the valleys."
    )
    val targetSentence = challengingPhrases[activePhraseIndex]
    
    var customTargetText by remember { mutableStateOf("") }
    val finalTargetSentence = customTargetText.trim().ifBlank { targetSentence }

    // TextToSpeech initialization
    var tts: android.speech.tts.TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val ttsEngine = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale.US
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    // TTS speaker SharedFlow connector
    val ttsTrigger by viewModel.ttsSpeakTrigger.collectAsStateWithLifecycle(initialValue = "")
    LaunchedEffect(ttsTrigger) {
        if (ttsTrigger.isNotBlank()) {
            tts?.speak(
                ttsTrigger,
                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
    }

    // SpeechRecognizer setup
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer: android.speech.SpeechRecognizer? by remember { mutableStateOf(null) }
    
    fun startGoogleVoiceListening() {
        try {
            val recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Accent Practice: Repeat the phrase clearly.")
            }
            recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    recognizer.destroy()
                }
                override fun onResults(results: android.os.Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        viewModel.evaluateUserSpeech(spokenText, finalTargetSentence)
                    }
                    recognizer.destroy()
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            speechRecognizer = recognizer
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
        }
    }

    fun startGoogleVoiceConversationalListening() {
        try {
            val recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Talk to LLM Coach: Say anything!")
            }
            recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    recognizer.destroy()
                }
                override fun onResults(results: android.os.Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        viewModel.evaluateAndChatSpokenUserSpeech(spokenText)
                    }
                    recognizer.destroy()
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            speechRecognizer = recognizer
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (currentMode == 0) {
                startGoogleVoiceListening()
            } else {
                startGoogleVoiceConversationalListening()
            }
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Spoken Coach Logo",
                        tint = CyanPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Spoken Hub",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("spoken_hub_title")
                    )
                    Text(
                        text = "American Accent Practice & Phonic Recalibrator",
                        color = MutedSlate,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        if (!isModelDownloaded) {
            item {
                // MODEL DOWNLOAD GATE CARD
                Card(
                    colors = CardColors(
                        containerColor = CharcoalSlate,
                        contentColor = Color.White,
                        disabledContainerColor = CharcoalSlate,
                        disabledContentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, SophisticatedOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Model Download Required",
                            color = CyanPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To enable local speech phonological processing and mother tongue influence (MTI) analysis securely on-device, you need to download the specialized Llama-3 English Spoken Practice model weights first.",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Specs
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Model: Llama-3-SpeechAccentPractice-GGUF", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text("Size: 3.82 GB", color = MutedSlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("Language Support: Indian Dialect Phonics Optimizer", color = MutedSlate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val progress = speechModel?.downloadProgress ?: 0
                        val isDownloading = progress > 0 && progress < 100

                        if (isDownloading) {
                            Text(
                                text = "Downloading: $progress% (${speechModel?.downloadSpeedMbs ?: 0.0} MB/s)",
                                color = CyanPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = progress.toFloat() / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanPrimary,
                                trackColor = CharcoalSurface,
                            )
                        } else {
                            Button(
                                onClick = {
                                    viewModel.startModelDownload(targetModelId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepPurpleContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("download_speech_model_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Download")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download Capable LLM Model", color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // MAIN VIEWPORT WHEN LOADED
            item {
                // MODEL IS LOADED STATUS
                Card(
                    colors = CardColors(
                        containerColor = CharcoalSlate,
                        contentColor = Color.White,
                        disabledContainerColor = CharcoalSlate,
                        disabledContentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, SophisticatedOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AlertGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Model Loaded: Llama-3 Accent Coach (3.82 GB)",
                            color = AlertGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            item {
                // PARAMETERS SELECTION (Native background region selector)
                Card(
                    colors = CardColors(
                        containerColor = CharcoalSlate,
                        contentColor = Color.White,
                        disabledContainerColor = CharcoalSlate,
                        disabledContentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, SophisticatedOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Native Accent Environment Background:",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Select Native Region language
                        val indianLangs = listOf("Hindi", "Bengali", "Tamil", "Telugu")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(indianLangs) { lang ->
                                val isSel = spokenLanguage == lang
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.updateSpokenLanguage(lang) },
                                    label = { Text(lang, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepPurpleContainer,
                                        selectedLabelColor = CyanPrimary,
                                        containerColor = CharcoalDark,
                                        labelColor = MutedSlate
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSel,
                                        borderColor = if (isSel) CyanPrimary else SophisticatedOutline
                                    ),
                                    modifier = Modifier.testTag("lang_chip_$lang").height(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // INTERACTIVE TAB SELECTOR (1: PRACTISE WORDS, 2: CONVERSATION TRAINER)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { viewModel.updateSpokenMode(0) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("practice_tab_btn"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentMode == 0) DeepPurpleContainer else CharcoalSlate
                        ),
                        border = BorderStroke(1.dp, if (currentMode == 0) CyanPrimary else SophisticatedOutline)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Practice Cards",
                                    tint = if (currentMode == 0) CyanPrimary else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Practice Phrases",
                                    color = if (currentMode == 0) Color.White else MutedSlate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Card(
                        onClick = { viewModel.updateSpokenMode(1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("conversation_tab_btn"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentMode == 1) DeepPurpleContainer else CharcoalSlate
                        ),
                        border = BorderStroke(1.dp, if (currentMode == 1) CyanPrimary else SophisticatedOutline)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share, 
                                    contentDescription = "AI spoken conversation",
                                    tint = if (currentMode == 1) CyanPrimary else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Talk to LLM Coach",
                                    color = if (currentMode == 1) Color.White else MutedSlate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            if (currentMode == 0) {
                // ================= PRACTICE PHRASES MODE (TAB 0) =================
                item {
                    Card(
                        colors = CardColors(
                            containerColor = CharcoalSlate,
                            contentColor = Color.White,
                            disabledContainerColor = CharcoalSlate,
                            disabledContentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select target sentence to practice",
                                color = CyanPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                challengingPhrases.forEachIndexed { idx, ph ->
                                    val isPhraseSel = activePhraseIndex == idx && customTargetText.isBlank()
                                    Card(
                                        onClick = {
                                            customTargetText = ""
                                            activePhraseIndex = idx
                                            viewModel.resetSpokenState()
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPhraseSel) CharcoalSurface else CharcoalDark
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isPhraseSel) CyanPrimary else SophisticatedOutline
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = ph,
                                            color = if (isPhraseSel) Color.White else MutedSlate,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Or enter your own custom phrase below:", color = MutedSlate, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customTargetText,
                                onValueChange = {
                                    customTargetText = it
                                    viewModel.resetSpokenState()
                                },
                                placeholder = { Text("Type any custom English phrase to practice...", color = MutedSlate, fontSize = 13.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("custom_phrase_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = SophisticatedOutline,
                                    focusedContainerColor = CharcoalDark,
                                    unfocusedContainerColor = CharcoalDark
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                item {
                    // COACHING WORKSPACE
                    Card(
                        colors = CardColors(
                            containerColor = CharcoalSlate,
                            contentColor = Color.White,
                            disabledContainerColor = CharcoalSlate,
                            disabledContentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "COACHING WORKSPACE",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalDark)
                                    .border(BorderStroke(1.dp, SophisticatedOutline))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = finalTargetSentence,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = {
                                            tts?.speak(
                                                finalTargetSentence,
                                                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .height(36.dp)
                                            .testTag("listen_american_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Speaker",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Listen with American Accent",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (isEvaluating) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AegisProgressIndicator(color = CyanPrimary, size = 32.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Analyzing Pronunciation Phonemes...",
                                        color = CyanPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        IconButton(
                                            onClick = {
                                                if (isListening) {
                                                    stopListening()
                                                } else {
                                                    recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(32.dp))
                                                .background(if (isListening) AlertGreen else DeepPurpleContainer)
                                                .testTag("mic_recording_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share, 
                                                contentDescription = "Microphone",
                                                tint = if (isListening) CharcoalDark else Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isListening) "Listening..." else "Tap to Speak",
                                            color = if (isListening) AlertGreen else Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (isListening) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(6) {
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height((12..36).random().dp)
                                                    .background(AlertGreen, RoundedCornerShape(2.dp))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(SophisticatedOutline))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Emulator Voice Simulator Options:",
                                    color = MutedSlate,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val accentedText = when {
                                                finalTargetSentence.contains("water") -> "Can I get a baht-al of vaa-ter, plees."
                                                finalTargetSentence.contains("schedule") -> "My workday she-dule is extremely comfortable."
                                                finalTargetSentence.contains("developer") -> "I am an offline Android software devel-opar."
                                                else -> "I am repeating the complete English text with heavy native accent."
                                            }
                                            viewModel.evaluateUserSpeech(accentedText, finalTargetSentence)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("sim_heavy_accent_btn")
                                    ) {
                                        Text("Heavy Indian Accent", fontSize = 10.sp, color = MutedSlate)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.evaluateUserSpeech(finalTargetSentence, finalTargetSentence)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("sim_american_accent_btn")
                                    ) {
                                        Text("American Accent Flow", fontSize = 10.sp, color = AlertGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                if (score != null) {
                    item {
                        Card(
                            colors = CardColors(
                                containerColor = CharcoalSlate,
                                contentColor = Color.White,
                                disabledContainerColor = CharcoalSlate,
                                disabledContentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, SophisticatedOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .testTag("evaluation_card"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Phonetic Score Card",
                                            color = CyanPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Pragmatic US Accent Alignment",
                                            color = MutedSlate,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        val scoreVal = score ?: 70
                                        val arcColor = if (scoreVal >= 85) AlertGreen else if (scoreVal >= 70) CyanPrimary else Color(0xFFFF5252)
                                        CircularProgressIndicator(
                                            progress = scoreVal.toFloat() / 100f,
                                            color = arcColor,
                                            trackColor = CharcoalSurface,
                                            modifier = Modifier.fillMaxSize(),
                                            strokeWidth = 5.dp
                                        )
                                        Text(
                                            text = "$scoreVal%",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(SophisticatedOutline))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "What We Heard:",
                                    color = MutedSlate,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$transcription\"",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DeepPurpleContainer.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "🇮🇳 Native Script Phonics Helper ($spokenLanguage background):",
                                            color = CyanPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = nativeHelp,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "American Accent Speech Therapy Tips:",
                                    color = CyanPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = accentTips,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "${spokenLanguage} MTI Trait Adjustments:",
                                    color = CyanPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mtiTraits,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // ================= INTERACTIVE SPOKEN VOICE CONVERSATION MODE (TAB 1) =================
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Speak back and forth with your AI Buddy. Have a spontaneous, safe conversation!",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Loop through dialogue history within LazyColumn loop reliably
                conversation.forEach { msg ->
                    item {
                        val isUser = msg.sender == "Human"
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                if (!isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(DeepPurpleContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("AI", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) DeepPurpleContainer else CharcoalSlate
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isUser) CyanPrimary else SophisticatedOutline
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 16.dp
                                    ),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.text,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                        
                                        if (!isUser) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        tts?.speak(msg.text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Read out loud",
                                                        tint = AlertGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text("Listen to Buddy", color = AlertGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }

                                        // DISPLAY USER SPEECH RESULTS DIRECTLY UNDER BUBBLE!
                                        if (isUser && msg.score != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyanPrimary.copy(alpha = 0.3f)))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if ((msg.score ?: 70) >= 80) AlertGreen else CyanPrimary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Pronunciation Match: ${msg.score}%",
                                                        color = CharcoalDark,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            if (!msg.accentTips.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("🗣️ American Accent Help:", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text(msg.accentTips ?: "", color = MutedSlate, fontSize = 11.sp, lineHeight = 15.sp)
                                            }

                                            if (!msg.mtiTraits.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("🇮🇳 MTI Adjustments:", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text(msg.mtiTraits ?: "", color = MutedSlate, fontSize = 11.sp, lineHeight = 15.sp)
                                            }

                                            if (!msg.nativeHelp.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("📖 Local Phonic Script Helper:", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text(msg.nativeHelp ?: "", color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                            }
                                        }
                                    }
                                }

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(CharcoalSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("ME", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isConvEvaluating) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AegisProgressIndicator(color = CyanPrimary, size = 28.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Phoneme analysis & coach is thinking...",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                item {
                    // CONVERSATIONAL DOCK CONTROLLERS
                    Card(
                        colors = CardColors(
                            containerColor = CharcoalSlate,
                            contentColor = Color.White,
                            disabledContainerColor = CharcoalSlate,
                            disabledContentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, SophisticatedOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CONVERSATION PANEL",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Mic Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = {
                                            if (isListening) {
                                                stopListening()
                                            } else {
                                                recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(if (isListening) AlertGreen else DeepPurpleContainer)
                                            .testTag("conversation_mic_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share, 
                                            contentDescription = "Speak to Buddy",
                                            tint = if (isListening) CharcoalDark else Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isListening) "Mic Active (Speak)..." else "Tap Mic & Speak",
                                        color = if (isListening) AlertGreen else Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isListening) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(8) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height((10..38).random().dp)
                                                .background(AlertGreen, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(SophisticatedOutline))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Dialogue Starters
                            Text(
                                text = "💡 Skip talking & test conversation topics:",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val conversationStarters = listOf(
                                "How has your day been?",
                                "What is the secret to a great accent?",
                                "Tell me a joke about robots."
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                conversationStarters.forEach { suggestion ->
                                    Card(
                                        onClick = {
                                            viewModel.evaluateAndChatSpokenUserSpeech(suggestion)
                                        },
                                        colors = CardDefaults.cardColors(containerColor = CharcoalDark),
                                        border = BorderStroke(1.dp, SophisticatedOutline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🗣️ \"$suggestion\"",
                                            color = CyanPrimary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dialogue Simulation controllers
                            Text(
                                text = "🤖 Emulator Dialect Accent Simulator:",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val accentedPhrase = "I like building mobile android software devel-opar."
                                        viewModel.evaluateAndChatSpokenUserSpeech(accentedPhrase)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("sim_heavy_indian_spoken_chat")
                                ) {
                                    Text("Heavy Indian Accent", fontSize = 10.sp, color = MutedSlate)
                                }

                                Button(
                                    onClick = {
                                        val perfectPhrase = "I am practicing my spontaneous conversational English daily."
                                        viewModel.evaluateAndChatSpokenUserSpeech(perfectPhrase)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("sim_perfect_american_spoken_chat")
                                ) {
                                    Text("American Accent Flow", fontSize = 10.sp, color = AlertGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Reset Chat History
                            Button(
                                onClick = { viewModel.clearSpokenConversation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5252)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("reset_spoken_chat_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear Conversation", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restart Spoken Conversation", color = Color(0xFFFF5252), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}
