# Renoir

[![Build and Test](https://github.com/exaclast/renoir/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/exaclast/renoir/actions/workflows/build-and-test.yml)
[![Release](https://github.com/exaclast/renoir/actions/workflows/release.yml/badge.svg)](https://github.com/exaclast/renoir/actions/workflows/release.yml)

A modern Android application built with **Jetpack Compose** and **Kotlin** that acts as an advanced Material You (Monet) theme designer, generator, and applier.

## Project Overview

This app allows users to visually design a Material 3 dynamic color scheme from a single seed color. It leverages the `material-kolor` library (a port of Google's `material-color-utilities`) to accurately map colors to the HCT (Hue, Chroma, Tone) color space and generate cohesive tonal palettes.

Once a theme is designed, the app generates a shell command that uses Android's internal `settings put secure theme_customization_overlay_packages` command to apply the theme system-wide. The app can apply this directly if granted the `WRITE_SECURE_SETTINGS` permission via ADB, or it can use **Shizuku** to apply the theme on-device without a PC.

## Permissions

To apply themes, you must grant the app permission using ONE of the following methods:

### Option 1: Shizuku (On-Device, No PC Required)

1. Install [Shizuku](https://shizuku.rikka.app/) from the Play Store.
2. Start the Shizuku service (via Wireless Debugging or root).
3. Open Renoir and click "Apply Theme". You will be prompted to grant Shizuku access.

### Option 2: ADB (Requires PC)

Connect your device to a computer with ADB installed and run:

```bash
adb shell pm grant com.exaclast.renoir android.permission.WRITE_SECURE_SETTINGS
```

_Note: This is a one-time setup. The permission persists across reboots and only needs to be granted once._

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Single-Activity, MVVM (Model-View-ViewModel) with StateFlow
- **Dynamic Colors**: `com.materialkolor:material-kolor` (HCT color space & Monet schemes)
- **Build System**: Gradle (Kotlin DSL, Version Catalogs)

## Key Components

The codebase is organized under `app/src/main/java/com/exaclast/renoir/`:

- **`MainActivity.kt`**: The entry point. Configures the edge-to-edge layout and wraps the app in a `DynamicMaterialTheme` that reacts to the `ThemeViewModel`.
- **`ThemeViewModel.kt`**: The single source of truth for the app's state (Seed Color, Palette Style, Dark/Light mode).
- **`ui/RenoirScreen.kt`**: The main dashboard. It combines the live preview and configuration controls.
- **`ui/components/ColorPicker.kt`**: A custom HSL (Hue, Saturation, Lightness) color picker with live gradient backgrounds for intuitive color selection.
- **`util/RenoirCommandGenerator.kt`**: Generates the exact JSON-formatted shell command required to apply the theme natively on Android 12+.
- **`util/ThemeApplier.kt`** & **`util/PermissionHelper.kt`**: Handles applying the theme directly via `Settings.Secure` if permissions are granted, or routes through Shizuku's IPC mechanism.

## How to Build and Run

To build the project locally or install it on a connected device/emulator, you can use the Gradle wrapper:

```bash
# Build the debug APK
./gradlew assembleDebug

# Install the app on a connected device
./gradlew installDebug
```

## Acknowledgements

This application was written and developed in collaboration with **Antigravity**, an AI coding assistant by Google DeepMind.
