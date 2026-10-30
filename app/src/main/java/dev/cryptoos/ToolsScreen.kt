package dev.cryptoos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf<CryptoTool?>(null) }
    var message by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedTool == null) {
            Text("Local tools", color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge)
            Text(
                "${CryptoTool.entries.size} offline utilities · inputs stay on this device",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 145.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(CryptoTool.entries, key = { it.name }) { tool ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedTool = tool },
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                            Text(tool.title, color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall)
                            Text(tool.hint, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        } else {
            ToolEditor(
                tool = selectedTool!!,
                onBack = { selectedTool = null },
                onCopy = { output ->
                    clipboard.setText(AnnotatedString(output))
                    message = "Result copied."
                }
            )
        }
    }

    if (message.isNotBlank()) {
        AlertDialog(onDismissRequest = { message = "" }, title = { Text("Local tools") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } })
    }
}

@Composable
private fun ToolEditor(tool: CryptoTool, onBack: () -> Unit, onCopy: (String) -> Unit) {
    var input by remember(tool) { mutableStateOf("") }
    var output by remember(tool) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(tool.title, color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium)
                Text("Runs locally · no network access",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        TextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Input") },
            placeholder = { Text(tool.hint) },
            minLines = 3,
            maxLines = 8
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { output = runCryptoTool(tool, input) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text("  Run")
            }
            OutlinedButton(onClick = { input = ""; output = "" }) { Text("Clear") }
            if (tool == CryptoTool.UUID) {
                OutlinedButton(onClick = { output = runCryptoTool(tool, input) }) { Text("New UUID") }
            }
        }
        if (output.isNotEmpty()) {
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Result", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium)
                    Text(output, color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onCopy(output) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text("  Copy result")
                    }
                }
            }
        }
    }
}
