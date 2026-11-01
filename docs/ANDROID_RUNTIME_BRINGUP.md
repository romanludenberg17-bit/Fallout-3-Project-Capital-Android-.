# Android runtime bring-up

## Current decision

Project Capital must not present the Kotlin folder picker as a game runtime. The Kotlin app is currently a prototype shell; it cannot execute `Fallout3.exe`, emulate the Windows registry, or run Gamebryo content.

The first runtime candidate is OpenNV / OpenNevada, a clean-room C# + Godot implementation that reads a player's own game installation. The upstream project contains explicit Fallout 3 content/runtime paths, including standalone Fallout 3 installation detection and Vault 101 opening work, but it is not yet a complete verified Fallout 3 campaign. The Android fork is primarily validated around its Android bring-up and New Vegas-oriented runtime, so Fallout 3 still needs device acceptance.

Pinned references reviewed on 2026-11-01:

- OpenNV upstream: `nikamigaming-create/OpenNV`
- Upstream commit: `4870bbd8a7ea3dce503f9553da12578a648d0096`
- Android fork: `openconstruct/OpenNevada-Android`
- Android fork commit: `d22b6c5ae5f83254e25ee9e311661299eb50da73`
- Godot .NET SDK: `4.7.2`
- Android fork SDK pin: .NET SDK `9.0.311`
- Android target framework in fork: `net9.0` (desktop target remains `net8.0`)

These are source pins for reproducible evaluation, not a claim that the two commits are interchangeable or that Fallout 3 works on Android.

## Integration strategy

1. Keep the user's game files external and read-only. Never commit executables, DLC, BSA archives, textures, audio, saves, or registry dumps.
2. Build the pinned Android runtime candidate in CI before vendoring or rewriting its source.
3. The current CI build consumes the pinned Android fork as a source dependency, then applies a Project Capital application identity overlay (`io.github.romanludenberg17.projectcapital`) before export.
4. Make Godot/OpenNV the actual game process. Retire the Kotlin launcher as the game entry point only after Android export, SAF access, input, rendering, and cold-start tests pass.
5. Add a SAF-to-runtime data provider. A `content://` URI is not a POSIX path; the runtime must read through Android document APIs or a verified private-storage staging layer. A string such as `Z:\\game\\` in preferences is not a mount.
6. Implement a runtime-owned compatibility registry provider only for code paths that genuinely query Windows registry data. It must be backed by the selected install profile and validated against the official GOG `beth_registry.cmd` script. Android itself has no Windows Registry.
7. Add touch input as Godot actions, including multi-touch look/movement, remapping, safe areas, and pause/menu focus.
8. Measure memory, frame pacing, shader compilation, cell transitions, saves, and crashes on real ARM64 devices.

## Acceptance gates

- Gate A: reproducible .NET build of the pinned Android fork.
- Gate B: reproducible Android ARM64 export with the matching Godot 4.7.2 Mono templates.
- Gate C: SAF-selected Fallout 3 install is admitted without copying or modifying retail files.
- Gate D: Fallout 3 data is parsed and Vault 101 opening scene is reached on Android.
- Gate E: player movement, camera, interaction, HUD, dialogue, combat, inventory, and save/continue are exercised.
- Gate F: repeatable performance/crash test on the target phone.

A green Kotlin launcher build only verifies the shell. A Godot launcher screen, menu render, or New Vegas test does not pass Fallout 3 gameplay gates. Until Gate E, releases must be labelled experimental and must not claim a complete native Fallout 3 port.

## Known integration risks

- OpenNV is under active development and its current Fallout 3 campaign is incomplete.
- The Android fork's documented device validation is not a full Fallout 3 gameplay acceptance.
- The upstream and Android fork currently use different repository heads; Android-specific changes need a reviewed forward-port.
- Godot Android C# export requires matching Mono editor/export templates, Android SDK/NDK, JDK and .NET tooling.
- The current Kotlin registry profile contains an assumed registry subkey. Do not treat it as authoritative until checked against the official GOG script and the runtime's actual registry access.
- Commercial game content remains user-supplied. Do not upload it to this public repository.

## Automated Android export

The workflow `.github/workflows/android-runtime-export.yml` checks out the pinned
Android candidate `openconstruct/OpenNevada-Android@d22b6c5ae5f83254e25ee9e311661299eb50da73`,
installs the matching Godot Mono toolchain and Android SDK, builds the Android
target, applies the Project Capital package identity, exports an ARM64 APK, and
verifies the final package identity and ABI before publishing a short-lived
Actions artifact.

The export-template installer uses Godot's Mono-specific `4.7.2.stable.mono`
template directory, matching the editor build. This keeps the CI check aligned
with the actual Mono editor lookup rules.

This is a source-built runtime candidate, not a wrapper around `Fallout3.exe`.
The remaining blocker is the Android content bridge: the runtime currently expects
a normal filesystem `--data-root`, while Android's Storage Access Framework returns
`content://` trees. The next implementation step is that bridge, followed by real
Fallout 3 Vault 101 device testing.
