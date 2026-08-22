# Monet Theme Picker

A modern Android application built with **Jetpack Compose** and **Kotlin** that acts as an advanced Material You (Monet) theme designer, generator, and applier.

## Project Overview

This app allows users to visually design a Material 3 dynamic color scheme from a single seed color. It leverages the `material-kolor` library (a port of Google's `material-color-utilities`) to accurately map colors to the HCT (Hue, Chroma, Tone) color space and generate cohesive tonal palettes. 

Once a theme is designed, the app generates a shell command that uses Android's internal `settings put secure theme_customization_overlay_packages` command to apply the theme system-wide. The app integrates directly with **Termux** via the `com.termux.RUN_COMMAND` intent to execute this shell command without requiring a PC, provided the user has granted Termux the necessary privileges.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Single-Activity, MVVM (Model-View-ViewModel) with StateFlow
- **Dynamic Colors**: `com.materialkolor:material-kolor` (HCT color space & Monet schemes)
- **Build System**: Gradle (Kotlin DSL, Version Catalogs)

## Key Components

The codebase is organized under `app/src/main/java/com/example/monetthemedesigner/`:

*   **`MainActivity.kt`**: The entry point. Configures the edge-to-edge layout and wraps the app in a `DynamicMaterialTheme` that reacts to the `ThemeViewModel`.
*   **`ThemeViewModel.kt`**: The single source of truth for the app's state (Seed Color, Palette Style, Dark/Light mode).
*   **`ui/MonetThemeDesignerScreen.kt`**: The main dashboard. It combines the live preview, configuration controls, and the Termux action card.
*   **`ui/components/ColorPicker.kt`**: A custom HSL (Hue, Saturation, Lightness) color picker with live gradient backgrounds for intuitive color selection.
*   **`util/MonetCommandGenerator.kt`**: Generates the exact JSON-formatted shell command required to apply the theme natively on Android 12+.
*   **`util/TermuxIntegration.kt`**: Handles formatting the intent to dispatch `com.termux.RUN_COMMAND`. Includes a clipboard fallback.

## How to Build and Run

To build the project locally or install it on a connected device/emulator, you can use the Gradle wrapper:

```bash
# Build the debug APK
./gradlew assembleDebug

# Install the app on a connected device
./gradlew installDebug
```


