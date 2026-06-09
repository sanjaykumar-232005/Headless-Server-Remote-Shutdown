package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN,
    CONNECTION_CREDENTIALS,
    ABOUT
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                // Ensure a pure slate-black (#0A0A0A) background is used globally for the sophisticated look
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0A0A)
                ) {
                    PowerControllerScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerControllerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val port by viewModel.port.collectAsStateWithLifecycle()
    val secretKey by viewModel.secretKey.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val serverResponse by viewModel.serverResponse.collectAsStateWithLifecycle()
    val onlineStatus by viewModel.onlineStatus.collectAsStateWithLifecycle()
    val isCheckingProgress by viewModel.isCheckingProgress.collectAsStateWithLifecycle()
    val incomingServerLog by viewModel.incomingServerLog.collectAsStateWithLifecycle()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

    // Scaffold with Sophisticated Dark Styling
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            // Elegant thin-border app bar adhering strictly to the template specification
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentScreen == AppScreen.MAIN) {
                                // High contrast server badge SVG icon (Red background overlay)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFDC2626).copy(alpha = 0.10f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(20.dp)) {
                                        // Top rack unit row
                                        drawRoundRect(
                                            color = Color(0xFFEF4444),
                                            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.40f),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                        // Left status indicator on top rack
                                        drawCircle(
                                            color = Color(0xFFEF4444),
                                            radius = 1.5.dp.toPx(),
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.20f)
                                        )
                                        
                                        // Bottom rack unit row
                                        drawRoundRect(
                                            color = Color(0xFFEF4444),
                                            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.40f),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.55f),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                        // Left status indicator on bottom rack
                                        drawCircle(
                                            color = Color(0xFFEF4444),
                                            radius = 1.5.dp.toPx(),
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.75f)
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "Server Controller",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF1F5F9), // Slate 100
                                    letterSpacing = (-0.2).sp
                                )
                            } else {
                                IconButton(
                                    onClick = { currentScreen = AppScreen.MAIN },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .testTag("back_button")
                                ) {
                                    Canvas(modifier = Modifier.size(18.dp)) {
                                        val strokeWidthPx = 2.dp.toPx()
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val halfLen = size.width * 0.4f
                                        
                                        drawLine(
                                            color = Color(0xFF94A3B8),
                                            start = androidx.compose.ui.geometry.Offset(cx + halfLen, cy),
                                            end = androidx.compose.ui.geometry.Offset(cx - halfLen, cy),
                                            strokeWidth = strokeWidthPx,
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = Color(0xFF94A3B8),
                                            start = androidx.compose.ui.geometry.Offset(cx - halfLen, cy),
                                            end = androidx.compose.ui.geometry.Offset(cx - halfLen + 6.dp.toPx(), cy - 6.dp.toPx()),
                                            strokeWidth = strokeWidthPx,
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = Color(0xFF94A3B8),
                                            start = androidx.compose.ui.geometry.Offset(cx - halfLen, cy),
                                            end = androidx.compose.ui.geometry.Offset(cx - halfLen + 6.dp.toPx(), cy + 6.dp.toPx()),
                                            strokeWidth = strokeWidthPx,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                                
                                Text(
                                    text = if (currentScreen == AppScreen.CONNECTION_CREDENTIALS) "Connection Credentials" else "About",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF1F5F9),
                                    letterSpacing = (-0.2).sp
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == AppScreen.MAIN) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .testTag("menu_button")
                                ) {
                                    Canvas(modifier = Modifier.size(20.dp)) {
                                        val r = 2.dp.toPx()
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val spacing = 6.dp.toPx()
                                        drawCircle(color = Color(0xFF94A3B8), radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy - spacing))
                                        drawCircle(color = Color(0xFF94A3B8), radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy))
                                        drawCircle(color = Color(0xFF94A3B8), radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy + spacing))
                                    }
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier
                                        .background(Color(0xFF111111))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Connection Credentials", color = Color(0xFFF1F5F9)) },
                                        onClick = {
                                            menuExpanded = false
                                            currentScreen = AppScreen.CONNECTION_CREDENTIALS
                                        },
                                        modifier = Modifier.testTag("menu_item_credentials")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("About", color = Color(0xFFF1F5F9)) },
                                        onClick = {
                                            menuExpanded = false
                                            currentScreen = AppScreen.ABOUT
                                        },
                                        modifier = Modifier.testTag("menu_item_about")
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0A0A),
                        titleContentColor = Color(0xFFF1F5F9)
                    )
                )
                // Thin border representing border-b border-white/5
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        // Ambient background glow centered dynamically around the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Ambience gradient layer (radial red light vignette)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.45f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFDC2626).copy(alpha = 0.08f), // html rgba(220,38,38,0.08)
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = size.width * 0.8f
                    ),
                    center = centerOffset,
                    radius = size.width * 0.8f
                )
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == AppScreen.MAIN) {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    } else {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "screen_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.MAIN -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 1. Sleek Server Address Info Badge/Status chip
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(100.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Neon green status dot with subtle pulsing animation
                                    val pulseLedTransition = rememberInfiniteTransition(label = "pulse_led_loop")
                                    val ledOpacity by pulseLedTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "led_pulse"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .scale(if (status == ServerStatus.READY) ledOpacity else 1f)
                                            .background(
                                                color = when (status) {
                                                    ServerStatus.READY -> Color(0xFF10B981)
                                                    ServerStatus.SENDING -> Color(0xFFF59E0B)
                                                    ServerStatus.COMMAND_SENT -> Color(0xFF3B82F6)
                                                    ServerStatus.FAILED -> Color(0xFFEF4444)
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = "$ipAddress : $port",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8), // slate-400
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 2. Central Power Control System
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                // Headless server title panel above button
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(bottom = 32.dp)
                                        .wrapContentSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ServerChassisIcon(
                                        color = Color(0xFF94A3B8).copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "HEADLESS SERVER",
                                        style = MaterialTheme.typography.labelSmall,
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B) // slate-500
                                    )
                                }

                                // Large circular power controller layout with concentric visual rings
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                ) {
                                    // Outer decorative ring line (w-60 h-60 equivalent in dynamic dp - approx 240dp)
                                    Box(
                                        modifier = Modifier
                                            .size(240.dp)
                                            .border(1.dp, Color.White.copy(alpha = 0.03f), CircleShape)
                                    )

                                    // Inner decorative ring line (w-52 h-52 equivalent in dynamic dp - approx 208dp)
                                    Box(
                                        modifier = Modifier
                                            .size(208.dp)
                                            .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape)
                                    )

                                    // The tactile Power button itself
                                    PowerButtonWidget(
                                        onClick = {
                                            focusManager.clearFocus()
                                            showConfirmDialog = true
                                        },
                                        enabled = status != ServerStatus.SENDING,
                                        status = status
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // 4. Reactive Status Panel (Header text status + status detail descriptions)
                                Text(
                                    text = when (status) {
                                        ServerStatus.READY -> "Ready"
                                        ServerStatus.SENDING -> "Sending..."
                                        ServerStatus.COMMAND_SENT -> {
                                            if (serverResponse == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                                                "Shutdown Initiated"
                                            } else {
                                                "Shutdown Failed"
                                            }
                                        }
                                        ServerStatus.FAILED -> "Shutdown Failed"
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        status == ServerStatus.COMMAND_SENT && serverResponse == "SHUTDOWN-INITIATED-SUCCESSFULLY" -> Color(0xFF10B981) // bright green
                                        status == ServerStatus.FAILED || (status == ServerStatus.COMMAND_SENT && serverResponse != "SHUTDOWN-INITIATED-SUCCESSFULLY") -> Color(0xFFEF4444) // error red
                                        else -> Color(0xFFF1F5F9) // slate-100
                                    },
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (status) {
                                        ServerStatus.READY -> "Touch button to initiate remote shutdown"
                                        ServerStatus.SENDING -> "Relaying authenticated handshake commands..."
                                        ServerStatus.COMMAND_SENT -> {
                                            if (serverResponse == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                                                "Shutdown Initiated"
                                            } else {
                                                "Shutdown Failed: Unauthorized or invalid command response"
                                            }
                                        }
                                        ServerStatus.FAILED -> errorMessage ?: "Handshake failed. Host unreachable."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        status == ServerStatus.COMMAND_SENT && serverResponse == "SHUTDOWN-INITIATED-SUCCESSFULLY" -> Color(0xFF10B981)
                                        status == ServerStatus.FAILED || (status == ServerStatus.COMMAND_SENT && serverResponse != "SHUTDOWN-INITIATED-SUCCESSFULLY") -> Color(0xFFEF4444)
                                        else -> Color(0xFF64748B)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }

                            // 3. Connection Status Indicator Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "SERVER AVAILABILITY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val statusColor = when (onlineStatus) {
                                                ServerOnlineStatus.ONLINE -> Color(0xFF10B981)
                                                ServerOnlineStatus.OFFLINE -> Color(0xFFEF4444)
                                                ServerOnlineStatus.CHECKING -> Color(0xFFF59E0B)
                                                ServerOnlineStatus.UNKNOWN -> Color(0xFF64748B)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(statusColor, CircleShape)
                                            )
                                            Text(
                                                text = when (onlineStatus) {
                                                    ServerOnlineStatus.ONLINE -> "ONLINE"
                                                    ServerOnlineStatus.OFFLINE -> "OFFLINE"
                                                    ServerOnlineStatus.CHECKING -> "CHECKING CONNECTIVITY..."
                                                    ServerOnlineStatus.UNKNOWN -> "STATUS UNKNOWN"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Text(
                                            text = "Auto-pings target host every 2 minutes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    // Manual diagnostic PING trigger button
                                    Button(
                                        onClick = { viewModel.checkOnlineStatus() },
                                        enabled = !isCheckingProgress,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.04f),
                                            contentColor = Color(0xFF94A3B8),
                                            disabledContainerColor = Color.White.copy(alpha = 0.01f),
                                            disabledContentColor = Color(0xFF64748B)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isCheckingProgress) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFFEF4444),
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 1.5.dp
                                                )
                                            } else {
                                                Canvas(modifier = Modifier.size(12.dp)) {
                                                    drawArc(
                                                        color = Color(0xFF94A3B8),
                                                        startAngle = 0f,
                                                        sweepAngle = 280f,
                                                        useCenter = false,
                                                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "CHECK NOW",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 5.5 Beautiful Animated Incoming Server Log Card
                            AnimatedVisibility(
                                visible = incomingServerLog != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Blue pulsing indicator for incoming data handshake
                                                val pulseTransition = rememberInfiniteTransition(label = "pulse_blue_loop")
                                                val logOpacity by pulseTransition.animateFloat(
                                                    initialValue = 0.5f,
                                                    targetValue = 1f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(1000, easing = LinearEasing),
                                                        repeatMode = RepeatMode.Reverse
                                                    ),
                                                    label = "blue_pulse"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .scale(logOpacity)
                                                        .background(Color(0xFF3B82F6), CircleShape)
                                                )
                                                Text(
                                                    text = "INCOMING HANDSHAKE (PORT $port)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Text(
                                                text = "DISMISS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier
                                                    .clickable { viewModel.clearIncomingLog() }
                                                    .padding(4.dp)
                                            )
                                        }
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                        Text(
                                            text = incomingServerLog ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // 6. Action status log console (expands dynamically when execution logs are triggered)
                            AnimatedVisibility(
                                visible = status == ServerStatus.FAILED || status == ServerStatus.COMMAND_SENT,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.40f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(
                                                            color = if (status == ServerStatus.FAILED) Color(0xFFFF2C46) else Color(0xFF10B981),
                                                            shape = CircleShape
                                                        )
                                                )
                                                Text(
                                                    text = if (status == ServerStatus.FAILED) "HANDSHAKE DIAGNOSTICS" else "SERVER REPLY SUCCESS",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (status == ServerStatus.FAILED) Color(0xFFFF2C46) else Color(0xFF10B981)
                                                )
                                            }
                                            
                                            // Reset badge trigger
                                            Text(
                                                text = "DISMISS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier
                                                    .clickable { viewModel.resetStatus() }
                                                    .padding(4.dp)
                                            )
                                        }

                                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                                        Text(
                                            text = errorMessage ?: serverResponse ?: "Successfully integrated communication payload.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.fillMaxWidth().testTag("log_output_text")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    AppScreen.CONNECTION_CREDENTIALS -> {
                        // Connection Credentials Editor flow
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Text(
                                        text = "CONNECTION CREDENTIALS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    // Target Node IP
                                    OutlinedTextField(
                                        value = ipAddress,
                                        onValueChange = { viewModel.updateIpAddress(it) },
                                        label = { Text("IP Address", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFEF4444),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                            focusedLabelColor = Color(0xFFEF4444),
                                            unfocusedLabelColor = Color(0xFF94A3B8),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("ip_input")
                                    )

                                    // Target Node Port
                                    OutlinedTextField(
                                        value = port,
                                        onValueChange = { viewModel.updatePort(it) },
                                        label = { Text("Port", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFEF4444),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                            focusedLabelColor = Color(0xFFEF4444),
                                            unfocusedLabelColor = Color(0xFF94A3B8),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("port_input")
                                    )

                                    // Private Pre-Shared Key input
                                    var isKeyVisible by remember { mutableStateOf(false) }
                                    OutlinedTextField(
                                        value = secretKey,
                                        onValueChange = { viewModel.updateSecretKey(it) },
                                        label = { Text("Secret Key", color = Color(0xFF64748B)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFEF4444),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                            focusedLabelColor = Color(0xFFEF4444),
                                            unfocusedLabelColor = Color(0xFF94A3B8),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        trailingIcon = {
                                            TextButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                Text(
                                                    text = if (isKeyVisible) "HIDE" else "SHOW",
                                                    color = Color(0xFFEF4444),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        },
                                        supportingText = {
                                            Text(
                                                text = "Securely hashed with SHA-256 before transmission",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("secret_key_input")
                                    )
                                }
                            }

                            Button(
                                onClick = { currentScreen = AppScreen.MAIN },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("apply_settings_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save & Return", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    AppScreen.ABOUT -> {
                        // About details panel Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color(0xFFDC2626).copy(alpha = 0.10f), RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(36.dp)) {
                                            drawRoundRect(
                                                color = Color(0xFFEF4444),
                                                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.40f),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color(0xFFEF4444),
                                                radius = 3.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.20f)
                                            )
                                            drawRoundRect(
                                                color = Color(0xFFEF4444),
                                                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.40f),
                                                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.55f),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color(0xFFEF4444),
                                                radius = 3.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.75f)
                                            )
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Shutdown Controller",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF1F5F9)
                                        )
                                        Text(
                                            text = "Version 1.0",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Created by",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B),
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "R S Sanjay Kumar",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFF1F5F9)
                                        )
                                    }

                                    Text(
                                        text = "© 2026 All Rights Reserved",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentScreen = AppScreen.MAIN },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("about_back_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.04f),
                                    contentColor = Color(0xFF94A3B8)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Text("Go Back", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

        // 7. Tactical Confirmation Alert Dialog for Shutdown action
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Confirm Shutdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                },
                text = {
                    Text(
                        text = "Are you sure you want to shut down the server at $ipAddress:$port? This action cannot be undone remotely.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            viewModel.sendShutdownCommand(
                                onSuccess = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "Handshake failed: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Shutdown", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color(0xFF111111),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}
}

/**
 * Beautiful glowing concentric Power button representing w-44 equivalent (~176.dp sizes).
 */
@Composable
fun PowerButtonWidget(
    onClick: () -> Unit,
    enabled: Boolean,
    status: ServerStatus,
    modifier: Modifier = Modifier
) {
    // Subtle breathing animation during idle/sending phases
    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse_loop")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "power_pulse_scale"
    )

    // Button Base gradient from red-500 (#EF4444) to red-800 (#991B1B)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEF4444),
            Color(0xFF991B1B)
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(176.dp) // Perfect w-44 (~176dp) proportions
            .scale(if (status == ServerStatus.SENDING) breathingScale else 1.0f)
            .background(
                brush = Brush.radialGradient(
                    colors = if (enabled) {
                        listOf(Color(0xFFDC2626).copy(alpha = 0.35f), Color.Transparent)
                    } else {
                        listOf(Color(0xFFDC2626).copy(alpha = 0.10f), Color.Transparent)
                    },
                ),
                shape = CircleShape
            )
            .padding(8.dp)
    ) {
        // Core interactive tactile button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(if (enabled) buttonGradient else Brush.linearGradient(listOf(Color(0x3FF1F5F9), Color(0x1F94A3B8))))
                .clickable(enabled = enabled, onClick = onClick)
                .testTag("power_button"),
            contentAlignment = Alignment.Center
        ) {
            // Elegant linear black shading overlay representing visual depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.08f))
            )

            if (status == ServerStatus.SENDING) {
                // Precision loading progress indicator on active transmit
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
            } else {
                // Vector drawn precise Power Symbol path
                Canvas(modifier = Modifier.size(40.dp)) {
                    val strokeWidthPx = 4.dp.toPx()
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = size.width * 0.38f

                    // Draw outer arc
                    // Start at -60 deg and sweep 300 degrees.
                    // This leaves a symmetric 60-degree gap at the top.
                    drawArc(
                        color = Color.White.copy(alpha = if (enabled) 1.0f else 0.4f),
                        startAngle = -60f,
                        sweepAngle = 300f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - radius, cy - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                    
                    // Draw vertical line indicator centered symmetrically in the opening
                    drawLine(
                        color = Color.White.copy(alpha = if (enabled) 1.0f else 0.4f),
                        start = androidx.compose.ui.geometry.Offset(cx, cy - radius * 1.0f),
                        end = androidx.compose.ui.geometry.Offset(cx, cy - radius * 0.1f),
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Custom rackmount graphic server chassis visualization matching Sophisticated Dark.
 */
@Composable
fun ServerChassisIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF94A3B8)
) {
    Column(
        modifier = modifier
            .width(64.dp)
            .height(58.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(3) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(4.dp))
                    .border(1.dp, color.copy(alpha = if (index == 0) 0.5f else 0.15f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hard disk controller slits
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.5.dp)
                                .background(color.copy(alpha = 0.15f), RoundedCornerShape(1.dp))
                        )
                    }
                }
                // Interactive micro status LEDs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = if (index == 0) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.10f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
