package dev.cryptoos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF17191D)
private val Panel = Color(0xFF22252B)
private val PanelRaised = Color(0xFF2B2F36)
private val Paper = Color(0xFFE7E2D9)
private val Muted = Color(0xFFA7A49E)
private val Brass = Color(0xFFC5A875)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoTheme {
                HomeScreen(
                    onOpenTerminal = {
                        startActivity(Intent(this, TerminalActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
private fun CryptoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Ink,
            surface = Panel,
            primary = Brass,
            onPrimary = Ink,
            onBackground = Paper,
            onSurface = Paper,
            secondary = Muted
        ),
        content = content
    )
}

private data class LauncherItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val enabled: Boolean = false
)

@Composable
private fun HomeScreen(onOpenTerminal: () -> Unit) {
    val items = listOf(
        LauncherItem("Terminal", "Local diagnostics", Icons.Default.Terminal, enabled = true),
        LauncherItem("Files", "Coming later", Icons.Default.Folder),
        LauncherItem("Settings", "Coming later", Icons.Default.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val line = Color.White.copy(alpha = 0.025f)
            var x = 0f
            while (x < size.width) {
                drawLine(line, start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(line, start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CryptoMark(modifier = Modifier.size(34.dp))
                        Spacer(Modifier.width(11.dp))
                        Text(
                            "CryptoOS",
                            color = Paper,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        )
                    }
                    Text(
                        "ANDROID WORKSPACE",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 1.6.sp
                    )
                }

                Spacer(Modifier.height(48.dp))
                Text(
                    "A quieter\nkind of desktop.",
                    color = Paper,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "A focused Android shell for local tools.",
                    color = Muted,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Brass.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CryptoMark(modifier = Modifier.size(23.dp), color = Brass)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("CryptoOS session", color = Paper, fontWeight = FontWeight.Medium)
                            Text("Local workspace · Linux runtime not configured",
                                color = Muted, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text("WORKSPACE", color = Muted, fontSize = 10.sp, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(216.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    items(items) { item ->
                        LauncherTile(
                            item = item,
                            onClick = if (item.enabled) onOpenTerminal else null
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Linux GUI app windows require an Android-integrated PRoot and Wayland runtime.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                "CRYPTOOS  ·  LOCAL-FIRST",
                color = Muted.copy(alpha = 0.72f),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun LauncherTile(item: LauncherItem, onClick: (() -> Unit)?) {
    val tileColor = if (item.enabled) PanelRaised else Panel.copy(alpha = 0.72f)
    val tile: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(17.dp))
                .background(tileColor)
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (item.enabled) Brass else Muted.copy(alpha = 0.55f),
                modifier = Modifier.size(23.dp)
            )
            Column {
                Text(item.title, color = if (item.enabled) Paper else Muted,
                    fontWeight = FontWeight.Medium)
                Text(item.subtitle, color = Muted, fontSize = 11.sp)
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = Modifier.height(100.dp),
            shape = RoundedCornerShape(17.dp),
            color = Color.Transparent,
            content = tile
        )
    } else {
        androidx.compose.material3.Surface(
            onClick = onClick,
            modifier = Modifier.height(100.dp),
            shape = RoundedCornerShape(17.dp),
            color = Color.Transparent,
            content = tile
        )
    }
}

@Composable
private fun CryptoMark(modifier: Modifier = Modifier, color: Color = Brass) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.04f)
            lineTo(size.width * 0.90f, size.height * 0.28f)
            lineTo(size.width * 0.90f, size.height * 0.72f)
            lineTo(size.width * 0.50f, size.height * 0.96f)
            lineTo(size.width * 0.10f, size.height * 0.72f)
            lineTo(size.width * 0.10f, size.height * 0.28f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = size.minDimension * 0.075f))
        drawLine(
            color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.50f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.50f),
            strokeWidth = size.minDimension * 0.075f
        )
        drawLine(
            color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.32f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.68f),
            strokeWidth = size.minDimension * 0.075f
        )
    }
}
