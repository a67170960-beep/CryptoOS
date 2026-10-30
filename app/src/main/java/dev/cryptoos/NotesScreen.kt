package dev.cryptoos

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable
fun NotesScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var notes by remember { mutableStateOf(CryptoStorage.loadNotes(context)) }
    var query by remember { mutableStateOf("") }
    var pinnedOnly by remember { mutableStateOf(false) }
    var tagFilter by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<CryptoNote?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<CryptoNote?>(null) }
    var message by remember { mutableStateOf("") }
    var createDialog by remember { mutableStateOf(false) }

    fun persist(updated: List<CryptoNote>) {
        notes = updated.sortedByDescending { it.updatedAt }
        CryptoStorage.saveNotes(context, notes)
    }

    val tags = notes.map { it.tag.trim() }.filter(String::isNotEmpty).distinct().sorted()
    val visibleNotes = notes
        .filter { !pinnedOnly || it.pinned }
        .filter { tagFilter.isBlank() || it.tag.equals(tagFilter, ignoreCase = true) }
        .filter {
            query.isBlank() || it.title.contains(query, true) ||
                it.body.contains(query, true) || it.tag.contains(query, true)
        }
        .sortedWith(compareByDescending<CryptoNote> { it.pinned }.thenByDescending { it.updatedAt })
    val totalWords = notes.sumOf { it.body.split(Regex("\\s+")).count(String::isNotBlank) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your notes", color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium)
                Text("${notes.size} notes · $totalWords words",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = {
                selected = null; title = ""; body = ""; tag = ""
                createDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  New")
            }
        }
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search title, text, or tag") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pinnedOnly) Button(onClick = { pinnedOnly = false }) { Text("Pinned only") }
            else OutlinedButton(onClick = { pinnedOnly = true }) { Text("All / pinned") }
            if (tagFilter.isNotBlank()) {
                OutlinedButton(onClick = { tagFilter = "" }) { Text("Tag: $tagFilter ×") }
            } else if (tags.isNotEmpty()) {
                var tagMenu by remember { mutableStateOf(false) }
                androidx.compose.foundation.layout.Box {
                    OutlinedButton(onClick = { tagMenu = true }) { Text("Filter by tag") }
                    DropdownMenu(expanded = tagMenu, onDismissRequest = { tagMenu = false }) {
                        tags.forEach { value ->
                            DropdownMenuItem(text = { Text(value) }, onClick = {
                                tagFilter = value; tagMenu = false
                            })
                        }
                    }
                }
            }
            OutlinedButton(onClick = {
                notes = notes.sortedByDescending { it.updatedAt }
                tagFilter = ""
            }) { Text("Recent") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleNotes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    onClick = { selected = note; title = note.title; body = note.body; tag = note.tag }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(note.title.ifBlank { "Untitled" },
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = {
                                persist(notes.map {
                                    if (it.id == note.id) it.copy(pinned = !it.pinned,
                                        updatedAt = System.currentTimeMillis()) else it
                                })
                            }) {
                                Icon(Icons.Default.PushPin,
                                    contentDescription = if (note.pinned) "Unpin" else "Pin",
                                    tint = if (note.pinned) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString("${note.title}\n\n${note.body}"))
                                message = "Note copied to clipboard."
                            }) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy note") }
                            IconButton(onClick = {
                                runCatching {
                                    context.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.body}")
                                        }, "Share note"
                                    ))
                                }.onFailure { message = "Unable to share this note." }
                            }) { Icon(Icons.Default.Share, contentDescription = "Share note") }
                        }
                        Text(note.body.ifBlank { "No text yet" }.take(180),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                listOfNotNull(note.tag.takeIf(String::isNotBlank), "${note.body.length} chars",
                                    "${note.body.split(Regex("\\s+")).count(String::isNotBlank)} words")
                                    .joinToString(" · "),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                    .format(Date(note.updatedAt)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            if (visibleNotes.isEmpty()) {
                item {
                    Text("No matching notes. Create a note to keep local ideas and snippets.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    if (selected != null || createDialog) {
        AlertDialog(
            onDismissRequest = { selected = null; createDialog = false },
            title = { Text(if (selected == null) "New note" else "Edit note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = title, onValueChange = { title = it },
                        label = { Text("Title") }, singleLine = true)
                    TextField(value = tag, onValueChange = { tag = it },
                        label = { Text("Tag") }, singleLine = true)
                    TextField(value = body, onValueChange = { body = it },
                        label = { Text("Note") }, minLines = 5, maxLines = 9)
                    Text("${body.length} characters · ${body.split(Regex("\\s+")).count(String::isNotBlank)} words",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val now = System.currentTimeMillis()
                    val original = selected
                    val updated = if (original == null) {
                        notes + CryptoNote(
                            id = java.util.UUID.randomUUID().toString(),
                            title = title.trim().ifBlank { "Untitled" },
                            body = body,
                            tag = tag.trim(),
                            pinned = false,
                            updatedAt = now
                        )
                    } else {
                        notes.map {
                            if (it.id == original.id) it.copy(
                                title = title.trim().ifBlank { "Untitled" },
                                body = body,
                                tag = tag.trim(),
                                updatedAt = now
                            ) else it
                        }
                    }
                    persist(updated)
                    selected = null
                    createDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (selected != null) {
                        TextButton(onClick = {
                            deleteTarget = selected
                            selected = null
                        }) { Text("Delete") }
                    }
                    TextButton(onClick = { selected = null; createDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    deleteTarget?.let { note ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this note?") },
            text = { Text("“${note.title}” will be removed from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    persist(notes.filterNot { it.id == note.id })
                    deleteTarget = null
                    selected = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
    if (message.isNotBlank()) {
        AlertDialog(onDismissRequest = { message = "" }, title = { Text("Notes") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } })
    }
}
