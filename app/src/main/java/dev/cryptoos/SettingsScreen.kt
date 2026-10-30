package dev.cryptoos

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    settings: CryptoSettings,
    onSettingsChange: (CryptoSettings) -> Unit
) {
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text("Personalize CryptoOS", color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge)
        Text("Changes save automatically on this device.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)

        Text("DESKTOP THEME", color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("graphite" to "Graphite", "midnight" to "Midnight", "umber" to "Umber")
                .forEach { (value, label) ->
                    if (settings.theme == value) Button(onClick = {
                        onSettingsChange(settings.copy(theme = value))
                    }) { Text(label) }
                    else OutlinedButton(onClick = {
                        onSettingsChange(settings.copy(theme = value))
                    }) { Text(label) }
                }
        }

        Text("ACCENT", color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("brass" to "Brass", "sage" to "Sage", "blue" to "Blue", "copper" to "Copper")
                .forEach { (value, label) ->
                    if (settings.accent == value) Button(onClick = {
                        onSettingsChange(settings.copy(accent = value))
                    }) { Text(label) }
                    else OutlinedButton(onClick = {
                        onSettingsChange(settings.copy(accent = value))
                    }) { Text(label) }
                }
        }

        SettingToggle("Show date and time", settings.showClock) {
            onSettingsChange(settings.copy(showClock = it))
        }
        SettingToggle("Show pinned shortcuts first", settings.pinnedFirst) {
            onSettingsChange(settings.copy(pinnedFirst = it))
        }
        SettingToggle("Compact home tiles", settings.compactCards) {
            onSettingsChange(settings.copy(compactCards = it))
        }
        SettingToggle("Haptic feedback", settings.haptics) {
            onSettingsChange(settings.copy(haptics = it))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Home grid: ${settings.columns} columns",
                color = MaterialTheme.colorScheme.onSurface)
            Row {
                OutlinedButton(onClick = {
                    onSettingsChange(settings.copy(columns = 2))
                }) { Text("2") }
                OutlinedButton(onClick = {
                    onSettingsChange(settings.copy(columns = 3))
                }, modifier = Modifier.padding(start = 6.dp)) { Text("3") }
            }
        }

        Column {
            Text("Text scale · ${(settings.fontScale * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onSurface)
            Slider(
                value = settings.fontScale,
                onValueChange = { onSettingsChange(settings.copy(fontScale = it)) },
                valueRange = 0.85f..1.2f
            )
        }

        Button(onClick = {
            val text = CryptoStorage.settingsJson(settings)
            runCatching {
                context.startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }, "Export CryptoOS settings"
                ))
            }.onFailure { message = "Unable to export settings." }
        }) { Text("Export settings") }

        OutlinedButton(onClick = {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                })
            }.onFailure { message = "Unable to open Android app settings." }
        }) { Text("Open Android app settings") }

        OutlinedButton(onClick = { confirmReset = true }) { Text("Reset preferences") }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset preferences?") },
            text = { Text("CryptoOS will return to its default theme and layout. Notes and files will remain.") },
            confirmButton = {
                TextButton(onClick = {
                    CryptoStorage.resetSettings(context)
                    onSettingsChange(CryptoSettings())
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
    if (message.isNotBlank()) {
        AlertDialog(onDismissRequest = { message = "" }, title = { Text("Settings") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } })
    }
}

@Composable
private fun SettingToggle(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
