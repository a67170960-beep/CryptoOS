# CryptoOS

CryptoOS is a native Android local-workspace shell built with Kotlin and Jetpack Compose. Version 0.2 adds a home dashboard, app-private file manager, persistent notes, 23 offline utilities, appearance settings, and a read-only diagnostics terminal. Across its modules the app exposes more than 70 usable actions; the individual operations are listed below.

## Features

- **Home:** subtle CryptoOS logo wallpaper, live clock and date, shortcut search, pin/unpin shortcuts, pinned-first ordering, 2- or 3-column layout, compact cards, three dark themes, four accent colors, live notes/workspace counters, five module shortcuts, and optional haptic feedback.
- **Files:** browse app-private folders, move to parent, search, filter all/files/folders, sort by name/size/date/type, show/hide hidden entries, create text files/folders, preview text, copy/share text, rename, delete with confirmation, and inspect workspace storage use.
- **Notes:** create/edit/delete, add tags, pin/unpin, search, filter by pinned/tag, sort by most recent, view character/word counts, copy/share notes, and keep notes on-device between sessions.
- **Offline tools (23):** calculator, percentage calculator, word and character counts, uppercase/lowercase/title case, reverse text, trim lines, remove blank lines, unique lines, sort lines, JSON format/compact, URL encode/decode, Base64 encode/decode, Unix timestamp conversion, length and temperature conversion, color contrast, and UUID generation.
- **Settings:** theme, accent, grid columns, compact cards, live clock, pinned-first order, haptic feedback, text scale, export settings, open Android app settings, and reset preferences.
- **Terminal (16 built-in commands):** help, about, date, time, uptime, Android version, device model, ABI, SDK, app version, sandbox identity, private working directory, file listing, storage status, command history, and clear.

All notes and files stay in the app's private storage. Utilities run locally. The terminal only supports its built-in diagnostics; it does not execute arbitrary shell commands.

The Termux script prepares Debian PRoot separately. This Android app does not yet embed a PRoot runtime or connect Wayland GUI apps to Android windows.

## Build

Requires JDK 17, Android SDK platform 35, and Gradle 8.9.

```bash
gradle assembleDebug
gradle assembleRelease
```

Without signing variables, the release APK is unsigned. To sign locally, set:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## PRoot setup

Install Termux from a trusted source, then run:

```bash
bash scripts/init-proot.sh
```

To install the privacy-oriented local file inspection dependencies inside Debian:

```bash
proot-distro login debian --user root -- bash -s < scripts/setup_osint.sh
```

`setup_osint.sh` installs ExifTool, Requests, and Beautiful Soup. It intentionally does not install automated username, email, or phone-number discovery tools.

## GitHub Actions

The workflow builds a release APK on pushes and creates a GitHub Release for tags beginning with `v`.

For signed APKs, add these optional repository secrets in GitHub:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Without signing secrets, the workflow builds an unsigned APK.
