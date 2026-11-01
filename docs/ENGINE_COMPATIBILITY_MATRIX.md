# Fallout 3 compatibility matrix

This matrix prevents New Vegas support or launcher validation from being mistaken for Fallout 3 support.

| Area | Current evidence | Project Capital status |
| --- | --- | --- |
| Android ARM64 Godot startup | Android fork reports a debug APK and launcher validation on one device | Candidate only; reproduce in CI/device |
| Fallout 3 plugin/archive admission | OpenNV upstream documents direct source transport and bounded FO3 work | Not yet verified in this project |
| Vault 101 opening | Upstream has bounded FO3 opening work and a dedicated contract probe | Must reproduce on Android |
| Full Capital Wasteland campaign | Upstream explicitly says FO3 is not a complete playable campaign | Not implemented |
| Touch controls | Kotlin editor prototype exists | Must connect to runtime input actions |
| Registry compatibility | Kotlin stores a virtual profile in preferences | No runtime registry adapter yet |
| SAF access | Kotlin selects a document tree | Godot runtime provider/staging bridge not implemented |
| Save/continue | Android fork documents shared save owners for its runtime | FO3 Android cold-continue not verified |
| FPS/RAM/crash telemetry | No reproducible FO3 Android measurements | Not tested |

## Release language

- **Launcher prototype:** can select and inspect a local game folder.
- **Runtime bring-up:** Godot runtime starts on Android.
- **FO3 data boot:** FO3 content is admitted and opening scene initializes.
- **Playable opening:** controls, interaction, UI and save/continue work in the opening.
- **Native port:** only claim after broad campaign systems and repeatable device acceptance.

Never label a build as a complete Fallout 3 Android port based only on launcher startup or New Vegas gameplay.
