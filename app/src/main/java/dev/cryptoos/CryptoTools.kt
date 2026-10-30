package dev.cryptoos

import android.graphics.Color as AndroidColor
import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class CryptoTool(val title: String, val hint: String) {
    CALCULATOR("Calculator", "Example: (12 + 8) * 2"),
    PERCENTAGE("Percentage", "Example: 15, 240"),
    WORD_COUNT("Word count", "Enter text"),
    CHARACTER_COUNT("Character count", "Enter text"),
    UPPERCASE("Uppercase", "Enter text"),
    LOWERCASE("Lowercase", "Enter text"),
    TITLE_CASE("Title case", "Enter text"),
    REVERSE_TEXT("Reverse text", "Enter text"),
    TRIM_LINES("Trim lines", "Enter text"),
    REMOVE_BLANKS("Remove blank lines", "Enter text"),
    UNIQUE_LINES("Unique lines", "Enter text"),
    SORT_LINES("Sort lines", "Enter text"),
    JSON_FORMAT("Format JSON", "Paste JSON"),
    JSON_COMPACT("Compact JSON", "Paste JSON"),
    URL_ENCODE("URL encode", "Enter text"),
    URL_DECODE("URL decode", "Enter encoded text"),
    BASE64_ENCODE("Base64 encode", "Enter text"),
    BASE64_DECODE("Base64 decode", "Enter Base64"),
    TIMESTAMP("Unix timestamp", "Seconds or milliseconds"),
    LENGTH("Length conversion", "Meters"),
    TEMPERATURE("Temperature conversion", "Celsius"),
    COLOR_CONTRAST("Color contrast", "#111111 #FFFFFF"),
    UUID("UUID generator", "No input required")
}

fun runCryptoTool(tool: CryptoTool, raw: String): String {
    val input = raw.trim()
    return try {
        when (tool) {
            CryptoTool.CALCULATOR -> Calculator(input).calculate().formatNumber()
            CryptoTool.PERCENTAGE -> {
                val values = input.split(Regex("[,;\\s]+")).filter(String::isNotBlank)
                require(values.size == 2) { "Enter percentage and amount, e.g. 15, 240." }
                val percent = values[0].toDouble()
                val amount = values[1].toDouble()
                "${(percent * amount / 100).formatNumber()} ($percent% of $amount)"
            }
            CryptoTool.WORD_COUNT -> "${input.split(Regex("\\s+")).count(String::isNotBlank)} words"
            CryptoTool.CHARACTER_COUNT -> "${raw.length} characters · ${input.length} without edge spaces"
            CryptoTool.UPPERCASE -> raw.uppercase(Locale.getDefault())
            CryptoTool.LOWERCASE -> raw.lowercase(Locale.getDefault())
            CryptoTool.TITLE_CASE -> raw.lowercase(Locale.getDefault()).split(Regex("\\s+"))
                .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }
            CryptoTool.REVERSE_TEXT -> raw.reversed()
            CryptoTool.TRIM_LINES -> raw.lineSequence().map(String::trim).joinToString("\n")
            CryptoTool.REMOVE_BLANKS -> raw.lineSequence().filter(String::isNotBlank).joinToString("\n")
            CryptoTool.UNIQUE_LINES -> raw.lineSequence().distinct().joinToString("\n")
            CryptoTool.SORT_LINES -> raw.lineSequence().sortedBy(String::lowercase).joinToString("\n")
            CryptoTool.JSON_FORMAT -> jsonRoundTrip(raw, pretty = true)
            CryptoTool.JSON_COMPACT -> jsonRoundTrip(raw, pretty = false)
            CryptoTool.URL_ENCODE -> URLEncoder.encode(raw, StandardCharsets.UTF_8.name())
            CryptoTool.URL_DECODE -> URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
            CryptoTool.BASE64_ENCODE ->
                Base64.encodeToString(raw.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            CryptoTool.BASE64_DECODE ->
                String(Base64.decode(input, Base64.DEFAULT), StandardCharsets.UTF_8)
            CryptoTool.TIMESTAMP -> {
                val value = input.toLongOrNull() ?: error("Enter a whole-number timestamp.")
                val millis = if (value in -9_999_999_999L..9_999_999_999L) value * 1000 else value
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault()).format(Date(millis))
            }
            CryptoTool.LENGTH -> {
                val meters = input.toDoubleOrNull() ?: error("Enter a number of meters.")
                "${meters.formatNumber()} m · ${(meters * 3.28084).formatNumber()} ft · " +
                    "${(meters / 1000).formatNumber()} km"
            }
            CryptoTool.TEMPERATURE -> {
                val celsius = input.toDoubleOrNull() ?: error("Enter a temperature in Celsius.")
                "${celsius.formatNumber()} °C · ${(celsius * 9 / 5 + 32).formatNumber()} °F · " +
                    "${(celsius + 273.15).formatNumber()} K"
            }
            CryptoTool.COLOR_CONTRAST -> contrast(input)
            CryptoTool.UUID -> UUID.randomUUID().toString()
        }
    } catch (error: Exception) {
        "Check the input: ${error.message ?: "invalid value"}"
    }
}

private fun jsonRoundTrip(input: String, pretty: Boolean): String {
    require(input.isNotBlank()) { "Paste a JSON object or array." }
    val indent = if (pretty) 2 else 0
    return when {
        input.trimStart().startsWith("{") -> org.json.JSONObject(input).toString(indent)
        input.trimStart().startsWith("[") -> org.json.JSONArray(input).toString(indent)
        else -> error("JSON must start with an object or array.")
    }
}

private fun contrast(input: String): String {
    val colors = input.split(Regex("\\s+")).filter(String::isNotBlank)
    require(colors.size == 2) { "Enter two hex colors separated by a space." }
    val first = luminance(AndroidColor.parseColor(colors[0]))
    val second = luminance(AndroidColor.parseColor(colors[1]))
    val ratio = (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    return "Contrast ratio ${"%.2f".format(Locale.US, ratio)}:1"
}

private fun luminance(color: Int): Double {
    fun channel(value: Int): Double {
        val normalized = value / 255.0
        return if (normalized <= 0.04045) normalized / 12.92
        else Math.pow((normalized + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * channel(AndroidColor.red(color)) +
        0.7152 * channel(AndroidColor.green(color)) +
        0.0722 * channel(AndroidColor.blue(color))
}

private fun Double.formatNumber(): String =
    if (isFinite() && this == toLong().toDouble()) toLong().toString()
    else String.format(Locale.getDefault(), "%.6f", this).trimEnd('0').trimEnd('.')

private class Calculator(private val source: String) {
    private var position = 0

    fun calculate(): Double {
        require(source.isNotBlank()) { "Enter a calculation." }
        val value = expression()
        skipSpaces()
        require(position == source.length) { "Unexpected character at position ${position + 1}." }
        require(value.isFinite()) { "The result is not finite." }
        return value
    }

    private fun expression(): Double {
        var value = term()
        while (true) {
            skipSpaces()
            value = when {
                consume('+') -> value + term()
                consume('-') -> value - term()
                else -> return value
            }
        }
    }

    private fun term(): Double {
        var value = factor()
        while (true) {
            skipSpaces()
            value = when {
                consume('*') -> value * factor()
                consume('/') -> {
                    val divisor = factor()
                    require(divisor != 0.0) { "Cannot divide by zero." }
                    value / divisor
                }
                else -> return value
            }
        }
    }

    private fun factor(): Double {
        skipSpaces()
        if (consume('+')) return factor()
        if (consume('-')) return -factor()
        if (consume('(')) {
            val value = expression()
            skipSpaces()
            require(consume(')')) { "Missing closing parenthesis." }
            return value
        }
        val start = position
        while (position < source.length &&
            (source[position].isDigit() || source[position] == '.')) position++
        require(position > start) { "Expected a number at position ${position + 1}." }
        return source.substring(start, position).toDoubleOrNull()
            ?: error("Invalid number at position ${start + 1}.")
    }

    private fun consume(char: Char): Boolean {
        if (position < source.length && source[position] == char) {
            position++
            return true
        }
        return false
    }

    private fun skipSpaces() {
        while (position < source.length && source[position].isWhitespace()) position++
    }
}
