package dev.cryptoos

import android.os.Bundle
import android.content.Context
import android.os.StatFs
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TerminalInk = Color(0xFF17191D)
private val TerminalPanel = Color(0xFF22252B)
private val TerminalText = Color(0xFFE7E2D9)
private val TerminalMuted = Color(0xFFA7A49E)
private val TerminalAccent = Color(0xFFC5A875)

class TerminalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TerminalScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

private data class TerminalLine(val command: String? = null, val output: String)

@Composable
private fun TerminalScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val history = remember {
        mutableStateListOf(
            TerminalLine(output = "CryptoOS local terminal · 15 built-in diagnostics."),
            TerminalLine(output = "No shell process or network connection is started.")
        )
    }
    var input by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(history.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalInk)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalPanel)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                    tint = TerminalText)
            }
            Column {
                Text("Terminal", color = TerminalText, fontWeight = FontWeight.Medium)
                Text("LOCAL DIAGNOSTICS", color = TerminalMuted, fontSize = 10.sp,
                    letterSpacing = 1.2.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEach { line ->
                line.command?.let {
                    Text("cryptoos:~$ $it", color = TerminalAccent,
                        fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
                Text(
                    line.output,
                    color = TerminalText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalPanel)
                .padding(start = 16.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("›", color = TerminalAccent, fontSize = 20.sp,
                fontFamily = FontFamily.Monospace)
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TerminalText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                decorationBox = { inner ->
                    if (input.isEmpty()) {
                        Text("Enter a local command", color = TerminalMuted, fontSize = 13.sp)
                    }
                    inner()
                }
            )
            IconButton(onClick = {
                val command = input.trim()
                if (command.isNotEmpty()) {
                    val priorCommands = history.mapNotNull { it.command }
                    val output = runLocalCommand(command, context, priorCommands)
                    if (command == "clear") {
                        history.clear()
                    } else {
                        history.add(TerminalLine(command = command, output = output))
                    }
                    input = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Run command",
                    tint = TerminalAccent)
            }
        }
    }
}

private fun runLocalCommand(command: String, context: Context, previous: List<String>): String {
    val root = context.filesDir
    val stat = StatFs(root.path)
    return when (command.lowercase(Locale.ROOT)) {
        "help" -> "Available: help, about, date, time, uptime, android, device, abi, sdk, app, whoami, pwd, ls, storage, history, clear"
        "about" -> "CryptoOS 0.2.0 · local diagnostics · commands are allow-listed."
        "date" -> SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).format(Date())
        "time" -> SimpleDateFormat("HH:mm:ss z", Locale.getDefault()).format(Date())
        "uptime" -> "${SystemClock.elapsedRealtime() / 1000} seconds since boot"
        "android" -> "Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}"
        "device" -> "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        "abi" -> android.os.Build.SUPPORTED_ABIS.joinToString()
        "sdk" -> "SDK ${android.os.Build.VERSION.SDK_INT}"
        "app" -> {
            val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                .ifNullOrBlank("unknown")
            "${context.packageName} · version $version"
        }
        "whoami" -> "CryptoOS app sandbox · no root access"
        "pwd" -> root.absolutePath
        "ls" -> "App-private files: " +
            (root.list()?.sorted()?.joinToString("  ").ifNullOrBlank("empty"))
        "storage" -> {
            val available = stat.availableBytes / (1024 * 1024)
            val total = stat.totalBytes / (1024 * 1024)
            "Private storage volume: $available MB available of $total MB"
        }
        "history" -> previous.takeLast(20).joinToString("\n").ifBlank { "No commands yet." }
        "clear" -> ""
        else -> "Command not available. This terminal only runs built-in, read-only local diagnostics."
    }
}

private fun String?.ifNullOrBlank(fallback: String): String = if (this.isNullOrBlank()) fallback else this
