# Fallout 3: Project Capital — Android

An experimental native Android porting project for **Fallout 3 (2008)**. The long-term goal is a real ARM64 Android engine/runtime port, not a Winlator/ExaGear wrapper.

> **Current status:** Android launcher prototype with SAF installation detection and virtual registry-profile groundwork. No Fallout engine/runtime is integrated yet; the game cannot be launched from this APK.

## Current prototype (0.2.0)
- Kotlin Android application, landscape-first UI.
- Selects a game directory through Android Storage Access Framework (SAF).
- Persists the folder permission.
- Detects the root layout for Fallout 3 (`Fallout3.exe` + `Data/`) and Fallout: New Vegas (`FalloutNV.exe` + `Data/`).
- Prepares a virtual registry profile for each detected title and stores it locally in app preferences.
- Keeps the launch action disabled until a real engine/runtime is integrated.
- GitHub Actions builds a debug APK.

## Registry compatibility design

The original Windows launchers look for a Bethesda registry value named `Installed Path`. For 64-bit Windows installations, the commonly documented locations are:

- Fallout 3: `HKLM\\SOFTWARE\\Wow6432Node\\Bethesda Softworks\\Fallout3`
- Fallout: New Vegas: `HKLM\\SOFTWARE\\Wow6432Node\\Bethesda Softworks\\FalloutNV`

The value is a `REG_SZ` named `Installed Path`, conventionally ending in a backslash. GOG's support articles document the missing-registry-key issue for both games. See [GOG Fallout 3 support](https://support.gog.com/hc/en-us/articles/115003498429-Fallout-3-Game-of-the-Year-Edition) and [GOG Fallout: New Vegas support](https://support.gog.com/hc/en-us/articles/115003633889-Fallout-New-Vegas-Greyed-out-options-or-missing-Play-button-in-the-launcher).

Android itself has no Windows Registry. Also, SAF gives the app a `content://` URI rather than a normal filesystem path. Therefore this prototype does **not** pretend to write real registry keys. It stores a local virtual profile with the selected SAF tree, the expected registry key/value, and a planned runtime mount point (`Z:\\game\\`). A future runtime adapter must:
1. mount the SAF tree at the agreed runtime path;
2. expose the corresponding Windows registry hive/key to the game or launcher;
3. return the correct mounted path for `Installed Path`;
4. verify the executable and required data files before launch.

Until that adapter and a compatible engine/runtime exist, the profile is configuration groundwork only. It does not by itself make either Windows executable run on Android.

## Architecture roadmap
1. Stabilize installation detection and persistent SAF access.
2. Complete touch controls and input mapping.
3. Select and validate a compatible Fallout engine codebase and its license.
4. Implement the Android/ARM64 platform layer: filesystem, registry compatibility, input, audio, threading, and graphics.
5. Add Vulkan rendering and validate against user-provided game data.
6. Test new game, save/load, cell transitions, frame pacing, memory use, and long-session stability.

## Build
Open in Android Studio (Ladybug or newer), or run **Android CI** in GitHub Actions and download the `project-capital-debug-apk` artifact.

## Legal / game files
No Fallout game assets or executables are included. Users must provide their own game files. Do not commit copyrighted game data to this repository.
