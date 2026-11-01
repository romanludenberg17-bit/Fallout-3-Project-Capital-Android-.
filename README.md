# Fallout 3: Project Capital — Android

An experimental native Android porting project for **Fallout 3 (2008)**.

> **Current status:** early Android launcher/prototype scaffold. This repository does **not** yet contain a Fallout 3 engine/runtime, and it cannot launch the Windows game. The goal is a genuine ARM64 Android implementation, not Wine/Winlator.

## Goals
- Native Android app, landscape-first 16:9 presentation.
- User-selected Fallout 3 installation directory via Android Storage Access Framework.
- Persist folder access and validate the expected game data layout.
- Touch-control editor and profiles (planned).
- ARM64 + Vulkan rendering backend (planned; requires an actual ported engine).
- Reproducible GitHub Actions APK builds.

## Current prototype
- Kotlin Android application.
- Landscape launcher UI.
- Selects a game folder using `ACTION_OPEN_DOCUMENT_TREE`.
- Persists the granted folder permission across launches.
- Checks for `Fallout3.exe` and a `Data` directory as a basic installation sanity check.
- Builds a debug APK in GitHub Actions.

## Important technical boundary
Android cannot execute Windows `.exe` files or `.bat` scripts natively and has no Windows Registry. The BAT/registry setup must be translated into Android-native configuration or implemented inside a future compatibility runtime. The prototype only detects the selected game installation; it does not execute Windows binaries.

## Build
Open this repository in Android Studio (Ladybug or newer), or use GitHub Actions:
1. Push to `main` or run **Android CI** manually.
2. Download the `project-capital-debug-apk` artifact from the workflow run.

## Legal / game files
No Fallout 3 game assets are included. Users must provide their own legally obtained game files. Do not commit copyrighted game data to this repository.

## Roadmap
1. Stabilize launcher, document-tree access, and install validation.
2. Add configurable touch controls and profiles.
3. Select and integrate a technically suitable Fallout 3 engine codebase.
4. Port platform, filesystem, audio, input, and graphics layers to Android/ARM64.
5. Implement Vulkan rendering and test against legally supplied game data.

A working APK from CI is only the launcher prototype until the engine/runtime port exists.
