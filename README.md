# Monet Theme Designer

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
*   **`ui/components/ColorPicker.kt`**: Allows the user to select the seed color via Hex input or preset color chips.
*   **`ui/components/TonalPaletteSwatch.kt`**: Visualizes the generated tone scales (Primary, Secondary, Tertiary).
*   **`util/MonetCommandGenerator.kt`**: Generates the exact JSON-formatted shell command required to apply the theme natively on Android 12+.
*   **`util/TermuxIntegration.kt`**: Handles formatting the intent to dispatch `com.termux.RUN_COMMAND`. Includes a clipboard fallback.

## Termux Integration Setup

To use the direct "Run in Termux" feature, you must configure Termux to allow external apps to run commands:
1. Install Termux.
2. Edit your `~/.termux/termux.properties` file in Termux to include: `allow-external-apps = true`.
3. Ensure Termux has root access (`su`) if you intend to apply secure settings directly on-device.

*Note: The app's `AndroidManifest.xml` already includes `<uses-permission android:name="com.termux.permission.RUN_COMMAND"/>`.*

## How to Build and Run

To build the project locally or install it on a connected device/emulator, you can use the Gradle wrapper:

```bash
# Build the debug APK
./gradlew assembleDebug

# Install the app on a connected device
./gradlew installDebug
```

## Next Steps / Future Work

If you are picking this project up in a new session, here are some areas for expansion:
1. **Extended Palette Styles**: Currently, the app maps `TonalSpot`, `Vibrant`, and `Expressive`. `material-kolor` (and Android 14+) supports additional styles like `Rainbow`, `FruitSalad`, `Monochrome`, and `Spritz`. You can re-enable these in `MonetCommandGenerator` when you have upgraded to a library version that fully supports them.
2. **Live Preview Enhancements**: The `LivePreviewArea` is currently a static layout of sample components. It can be expanded into a scrollable, comprehensive showcase of all Material 3 components (Navigation Bar, Modals, Sliders, Cards, etc.).
3. **Contrast Levels**: Implement the logic to pass contrast parameters (`contrastLevel`) to `DynamicMaterialTheme` to support Material 3's high/medium/standard contrast modes.
4. **Color Wheel UI**: Replace the basic Hex input and preset chips with a fully interactive HSV color wheel or gradient slider for more granular color picking.
