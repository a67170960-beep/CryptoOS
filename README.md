# CryptoOS

CryptoOS is an early native Android launcher shell built with Kotlin and Jetpack Compose. It includes a dark, Linux-inspired home screen, a small local diagnostics terminal, and a Termux PRoot bootstrap script.

## Current scope

- Package and application ID: `dev.cryptoos`
- Compose home screen with a minimal CryptoOS mark
- Local terminal commands: `help`, `about`, `date`, `uname`, `pwd`, `ls`, and `clear`
- Debian PRoot bootstrap for Termux
- Optional APK signing and GitHub Actions build/security workflow

The current terminal is deliberately limited to built-in, read-only device diagnostics. It does not execute arbitrary shell commands. The Android app does not yet embed a PRoot runtime or provide Linux GUI applications as Android windows. The bootstrap script prepares a Debian environment in Termux; it does not connect that environment to the app's window system.

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
