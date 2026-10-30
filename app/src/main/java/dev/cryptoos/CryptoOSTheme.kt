package dev.cryptoos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Graphite = Color(0xFF17191D)
private val GraphitePanel = Color(0xFF22252B)
private val Paper = Color(0xFFE7E2D9)
private val Muted = Color(0xFFA7A49E)

@Composable
fun CryptoOSTheme(settings: CryptoSettings, content: @Composable () -> Unit) {
    val background = when (settings.theme) {
        "midnight" -> Color(0xFF111820)
        "umber" -> Color(0xFF201B18)
        else -> Graphite
    }
    val surface = when (settings.theme) {
        "midnight" -> Color(0xFF1B2630)
        "umber" -> Color(0xFF2A231E)
        else -> GraphitePanel
    }
    val primary = when (settings.accent) {
        "sage" -> Color(0xFF9AAE91)
        "blue" -> Color(0xFF92AFC2)
        "copper" -> Color(0xFFC28F6E)
        else -> Color(0xFFC5A875)
    }
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primary,
            onPrimary = background,
            secondary = Muted,
            background = background,
            onBackground = Paper,
            surface = surface,
            onSurface = Paper,
            surfaceVariant = surface.copy(alpha = 0.82f),
            onSurfaceVariant = Muted
        ),
        content = content
    )
}
