package dev.cryptoos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class CryptoTab(val label: String) {
    HOME("Home"), FILES("Files"), NOTES("Notes"), TOOLS("Tools"), SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var settings by remember { mutableStateOf(CryptoStorage.loadSettings(context)) }
            var currentTab by rememberSaveable { mutableStateOf(CryptoTab.HOME) }
            val systemDensity = LocalDensity.current

            CryptoOSTheme(settings) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalDensity provides Density(systemDensity.density, settings.fontScale)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                CryptoTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                                        label = { Text(tab.label, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Column(
                            modifier = Modifier.fillMaxSize().padding(padding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CryptoMark(modifier = Modifier.size(25.dp))
                                Text("  CryptoOS", color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium)
                                Text("  LOCAL WORKSPACE",
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall)
                            }

                            when (currentTab) {
                                CryptoTab.HOME -> HomeScreen(
                                    settings = settings,
                                    onNavigate = { currentTab = it },
                                    onSettingsChange = {
                                        settings = it
                                        CryptoStorage.saveSettings(context, it)
                                    },
                                    onOpenTerminal = {
                                        startActivity(Intent(context, TerminalActivity::class.java))
                                    }
                                )
                                CryptoTab.FILES -> FileManagerScreen()
                                CryptoTab.NOTES -> NotesScreen()
                                CryptoTab.TOOLS -> ToolsScreen()
                                CryptoTab.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    onSettingsChange = {
                                        settings = it
                                        CryptoStorage.saveSettings(context, it)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun CryptoTab.icon(): ImageVector = when (this) {
    CryptoTab.HOME -> Icons.Default.Home
    CryptoTab.FILES -> Icons.Default.Folder
    CryptoTab.NOTES -> Icons.Default.Note
    CryptoTab.TOOLS -> Icons.Default.Build
    CryptoTab.SETTINGS -> Icons.Default.Settings
}

private data class HomeShortcut(
    val id: String,
    val title: String,
    val detail: String,
    val icon: ImageVector,
    val tab: CryptoTab? = null,
    val terminal: Boolean = false
)

@Composable
private fun HomeScreen(
    settings: CryptoSettings,
    onNavigate: (CryptoTab) -> Unit,
    onSettingsChange: (CryptoSettings) -> Unit,
    onOpenTerminal: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var search by remember { mutableStateOf("") }
    var pinned by remember { mutableStateOf(CryptoStorage.pinnedShortcuts(context)) }
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(30_000)
        }
    }
    val shortcuts = listOf(
        HomeShortcut("terminal", "Terminal", "16 local diagnostics", Icons.Default.Terminal, terminal = true),
        HomeShortcut("files", "Files", "Private workspace", Icons.Default.Folder, CryptoTab.FILES),
        HomeShortcut("notes", "Notes", "Offline notes", Icons.Default.Note, CryptoTab.NOTES),
        HomeShortcut("tools", "Tools", "${CryptoTool.entries.size} offline utilities", Icons.Default.Build, CryptoTab.TOOLS),
        HomeShortcut("settings", "Settings", "Personalize the shell", Icons.Default.Settings, CryptoTab.SETTINGS)
    )
    val query = search.trim()
    val visible = shortcuts
        .filter { query.isBlank() || it.title.contains(query, true) || it.detail.contains(query, true) }
        .let { list -> if (settings.pinnedFirst) list.sortedByDescending { it.id in pinned } else list }
    val notesCount = CryptoStorage.loadNotes(context).size
    val workspaceFiles = remember { CryptoStorage.workspace(context).list()?.size ?: 0 }

    Box(modifier = Modifier.fillMaxSize()) {
        CryptoMark(
            modifier = Modifier.align(Alignment.Center).size(280.dp),
            alpha = 0.045f
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("A quieter kind of desktop.", color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall)
                Text("70+ local actions, all in one place.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            if (settings.showClock) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall)
                    Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search CryptoOS shortcuts") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("NOTES", notesCount.toString(), Modifier.weight(1f))
            MetricCard("FILES", workspaceFiles.toString(), Modifier.weight(1f))
            MetricCard("FEATURES", "70+", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (pinned.isEmpty()) "Workspace" else "Pinned first",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("graphite", "midnight", "umber").forEach { theme ->
                    androidx.compose.material3.FilterChip(
                        selected = settings.theme == theme,
                        onClick = {
                            val changed = settings.copy(theme = theme)
                            onSettingsChange(changed)
                        },
                        label = { Text(theme.replaceFirstChar(Char::titlecase)) }
                    )
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(settings.columns),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(visible, key = { it.id }) { shortcut ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(if (settings.compactCards) 100.dp else 124.dp)
                        .clickable {
                            if (settings.haptics) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (shortcut.terminal) onOpenTerminal()
                            else shortcut.tab?.let(onNavigate)
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(shortcut.icon, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                if (settings.haptics) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                pinned = if (shortcut.id in pinned) pinned - shortcut.id
                                else pinned + shortcut.id
                                CryptoStorage.setPinnedShortcuts(context, pinned)
                            }) {
                                Icon(
                                    if (shortcut.id in pinned) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = if (shortcut.id in pinned) "Unpin" else "Pin",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(shortcut.title, color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall)
                            Text(shortcut.detail, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (visible.isEmpty()) {
                item {
                    Text("No matching shortcuts.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp))
                }
            }
        }
        Text("PRoot/Wayland GUI integration is not bundled in this build.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CryptoMark(modifier: Modifier = Modifier, alpha: Float = 1f) {
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val stroke = diameter * 0.075f
        val inset = stroke * 0.65f
        drawArc(
            color = tint,
            startAngle = 42f,
            sweepAngle = 276f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(diameter - inset * 2f, diameter - inset * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = tint,
            start = Offset(diameter * 0.49f, diameter * 0.5f),
            end = Offset(diameter * 0.77f, diameter * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawCircle(tint, radius = stroke * 0.72f, center = Offset(diameter * 0.5f, diameter * 0.5f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(13.dp)) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            Text(value, color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium)
        }
    }
}
