package dev.cryptoos

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private enum class FileFilter { ALL, FILES, FOLDERS }
private enum class FileSort { NAME, SIZE, MODIFIED, TYPE }

@Composable
fun FileManagerScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val workspace = remember { CryptoStorage.workspace(context).canonicalFile }
    var current by remember { mutableStateOf(workspace) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FileFilter.ALL) }
    var sort by remember { mutableStateOf(FileSort.NAME) }
    var showHidden by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    var createDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var createFolder by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<File?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<File?>(null) }
    var previewTarget by remember { mutableStateOf<File?>(null) }
    var message by remember { mutableStateOf("") }

    fun refreshNow() { refresh++ }
    fun validChild(name: String): File {
        require(name.isNotBlank() && !name.contains('/') && !name.contains('\\')) {
            "Use a simple name without path separators."
        }
        val target = File(current, name).canonicalFile
        require(target.parentFile == current.canonicalFile) { "The name leaves the workspace." }
        return target
    }

    val files = remember(current, query, filter, sort, showHidden, refresh) {
        (current.listFiles()?.toList() ?: emptyList())
            .filter { showHidden || !it.name.startsWith(".") }
            .filter { it.name.contains(query, ignoreCase = true) }
            .filter {
                when (filter) {
                    FileFilter.ALL -> true
                    FileFilter.FILES -> it.isFile
                    FileFilter.FOLDERS -> it.isDirectory
                }
            }
            .sortedWith(
                when (sort) {
                    FileSort.NAME -> compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
                    FileSort.SIZE -> compareBy<File> { !it.isDirectory }.thenBy { it.length() }
                    FileSort.MODIFIED -> compareByDescending<File> { it.lastModified() }
                    FileSort.TYPE -> compareBy<File> { !it.isDirectory }
                        .thenBy { it.extension.lowercase() }.thenBy { it.name.lowercase() }
                }
            )
    }
    val workspaceBytes = remember(refresh) { workspace.walkTopDown().filter(File::isFile).sumOf(File::length) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "  ${current.relativeTo(workspace).path.ifBlank { "Workspace" }}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text("${files.size} items", color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "App-private storage · ${formatBytes(workspaceBytes)} used",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Search this folder") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            IconButton(onClick = { sort = FileSort.entries[(sort.ordinal + 1) % FileSort.entries.size] }) {
                Icon(Icons.Default.Sort, contentDescription = "Sort: ${sort.name.lowercase()}")
            }
            IconButton(onClick = { createDialog = true; newName = ""; createFolder = false }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "Create")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FileFilter.entries.forEach { option ->
                if (filter == option) {
                    Button(onClick = { filter = option }) {
                        Text(option.name.lowercase().replaceFirstChar(Char::titlecase))
                    }
                } else {
                    OutlinedButton(onClick = { filter = option }) {
                        Text(option.name.lowercase().replaceFirstChar(Char::titlecase))
                    }
                }
            }
            TextButton(onClick = { showHidden = !showHidden }) {
                Text(if (showHidden) "Hide hidden" else "Show hidden")
            }
        }
        Text(
            "Sorted by ${sort.name.lowercase()} · tap sort to change",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (current != workspace) {
                item {
                    OutlinedButton(onClick = {
                        current = current.parentFile?.takeIf { it.path.startsWith(workspace.path) } ?: workspace
                    }) { Text("← Parent folder") }
                }
            }
            items(files, key = { it.absolutePath }) { file ->
                var menuExpanded by remember(file.absolutePath) { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (file.isDirectory) current = file.canonicalFile
                        else if (file.length() <= 256_000L) previewTarget = file
                        else message = "Preview is limited to files under 256 KB."
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text(file.name, color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (file.isDirectory) "Folder · ${file.list()?.size ?: 0} items"
                                else "${formatBytes(file.length())} · ${DateFormat.getDateTimeInstance(
                                    DateFormat.SHORT, DateFormat.SHORT
                                ).format(Date(file.lastModified()))}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "File actions")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (file.isFile) {
                                DropdownMenuItem(text = { Text("Preview") }, onClick = {
                                    menuExpanded = false; previewTarget = file
                                })
                                DropdownMenuItem(text = { Text("Copy text") }, onClick = {
                                    menuExpanded = false
                                    runCatching { clipboard.setText(AnnotatedString(file.readText().take(200_000))) }
                                        .onSuccess { message = "File text copied." }
                                        .onFailure { message = "This file cannot be copied as text." }
                                })
                                DropdownMenuItem(text = { Text("Share text") }, onClick = {
                                    menuExpanded = false
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, file.readText().take(200_000))
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share file text"))
                                    }.onFailure { message = "Unable to share this file." }
                                })
                            }
                            DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                menuExpanded = false; renameTarget = file; renameValue = file.name
                            })
                            DropdownMenuItem(text = { Text("Delete…") }, onClick = {
                                menuExpanded = false; deleteTarget = file
                            }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) })
                        }
                    }
                }
            }
            if (files.isEmpty()) {
                item {
                    Text("This folder is empty. Create a file or folder to get started.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp))
                }
            }
        }
    }

    if (createDialog) {
        AlertDialog(
            onDismissRequest = { createDialog = false },
            title = { Text(if (createFolder) "New folder" else "New text file") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = newName, onValueChange = { newName = it },
                        label = { Text("Name") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { createFolder = false }) { Text("Text file") }
                        TextButton(onClick = { createFolder = true }) { Text("Folder") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        val target = validChild(newName.trim())
                        val created = if (createFolder) target.mkdirs() else target.createNewFile()
                        require(created) { "A file with that name already exists." }
                    }.onSuccess { refreshNow(); createDialog = false }
                        .onFailure { message = it.message ?: "Could not create item." }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { createDialog = false }) { Text("Cancel") } }
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename") },
            text = { TextField(value = renameValue, onValueChange = { renameValue = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    runCatching {
                        val destination = validChild(renameValue.trim())
                        require(!destination.exists()) { "Another item already uses that name." }
                        require(target.renameTo(destination)) { "Rename failed." }
                    }.onSuccess { refreshNow(); renameTarget = null }
                        .onFailure { message = it.message ?: "Rename failed." }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            text = { Text("This removes the item from CryptoOS app storage.") },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { require(target.deleteRecursively()) { "Delete failed." } }
                        .onSuccess { refreshNow(); deleteTarget = null }
                        .onFailure { message = it.message ?: "Delete failed." }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    previewTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { previewTarget = null },
            title = { Text(target.name) },
            text = {
                Text(
                    runCatching { target.readText().take(16_000) }
                        .getOrElse { "Preview is available for readable text files only." },
                    maxLines = 24
                )
            },
            confirmButton = { TextButton(onClick = { previewTarget = null }) { Text("Close") } }
        )
    }

    if (message.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { message = "" },
            title = { Text("Workspace") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { message = "" }) { Text("OK") } }
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000 -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1_000_000.0)
    bytes >= 1_000 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1_000.0)
    else -> "$bytes B"
}
