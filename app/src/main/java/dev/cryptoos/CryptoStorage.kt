package dev.cryptoos

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class CryptoNote(
    val id: String,
    val title: String,
    val body: String,
    val tag: String,
    val pinned: Boolean,
    val updatedAt: Long
)

data class CryptoSettings(
    val theme: String = "graphite",
    val accent: String = "brass",
    val columns: Int = 2,
    val compactCards: Boolean = false,
    val showClock: Boolean = true,
    val pinnedFirst: Boolean = true,
    val haptics: Boolean = true,
    val fontScale: Float = 1f
)

object CryptoStorage {
    private const val PREFS = "cryptoos_local"
    private const val NOTES_KEY = "notes"
    private const val PINNED_KEY = "pinned_shortcuts"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun workspace(context: Context): File =
        File(context.filesDir, "workspace").apply { mkdirs() }

    fun loadSettings(context: Context): CryptoSettings {
        val p = prefs(context)
        return CryptoSettings(
            theme = p.getString("theme", "graphite") ?: "graphite",
            accent = p.getString("accent", "brass") ?: "brass",
            columns = p.getInt("columns", 2).coerceIn(2, 3),
            compactCards = p.getBoolean("compact_cards", false),
            showClock = p.getBoolean("show_clock", true),
            pinnedFirst = p.getBoolean("pinned_first", true),
            haptics = p.getBoolean("haptics", true),
            fontScale = p.getFloat("font_scale", 1f).coerceIn(0.85f, 1.2f)
        )
    }

    fun saveSettings(context: Context, settings: CryptoSettings) {
        prefs(context).edit()
            .putString("theme", settings.theme)
            .putString("accent", settings.accent)
            .putInt("columns", settings.columns.coerceIn(2, 3))
            .putBoolean("compact_cards", settings.compactCards)
            .putBoolean("show_clock", settings.showClock)
            .putBoolean("pinned_first", settings.pinnedFirst)
            .putBoolean("haptics", settings.haptics)
            .putFloat("font_scale", settings.fontScale.coerceIn(0.85f, 1.2f))
            .apply()
    }

    fun resetSettings(context: Context) {
        prefs(context).edit()
            .remove("theme")
            .remove("accent")
            .remove("columns")
            .remove("compact_cards")
            .remove("show_clock")
            .remove("pinned_first")
            .remove("haptics")
            .remove("font_scale")
            .apply()
    }

    fun pinnedShortcuts(context: Context): Set<String> =
        prefs(context).getStringSet(PINNED_KEY, emptySet())?.toSet() ?: emptySet()

    fun setPinnedShortcuts(context: Context, pinned: Set<String>) {
        prefs(context).edit().putStringSet(PINNED_KEY, pinned.toSet()).apply()
    }

    fun loadNotes(context: Context): List<CryptoNote> {
        val raw = prefs(context).getString(NOTES_KEY, "[]") ?: "[]"
        return try {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                CryptoNote(
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                    title = item.optString("title", "Untitled"),
                    body = item.optString("body"),
                    tag = item.optString("tag"),
                    pinned = item.optBoolean("pinned"),
                    updatedAt = item.optLong("updatedAt")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveNotes(context: Context, notes: List<CryptoNote>) {
        val json = JSONArray()
        notes.forEach { note ->
            json.put(
                JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("body", note.body)
                    .put("tag", note.tag)
                    .put("pinned", note.pinned)
                    .put("updatedAt", note.updatedAt)
            )
        }
        prefs(context).edit().putString(NOTES_KEY, json.toString()).apply()
    }

    fun settingsJson(settings: CryptoSettings): String =
        JSONObject()
            .put("theme", settings.theme)
            .put("accent", settings.accent)
            .put("columns", settings.columns)
            .put("compactCards", settings.compactCards)
            .put("showClock", settings.showClock)
            .put("pinnedFirst", settings.pinnedFirst)
            .put("haptics", settings.haptics)
            .put("fontScale", settings.fontScale.toDouble())
            .toString(2)
}
