# Engine evaluation: Fallout 3 runtime

## Decision

**OpenNV is the first engine candidate to evaluate, not an integrated dependency and not a finished Fallout 3 port.**

Upstream: https://github.com/nikamigaming-create/OpenNV  
Android investigation fork: https://github.com/openconstruct/OpenNevada-Android  
Project site / compatibility notes: https://opennevada.com/

The upstream OpenNV README describes a clean-room C# + Godot runtime. Its current status says New Vegas has the strongest live route, while Fallout 3 has direct source transport and bounded Vault 101 opening work; neither campaign is described as a complete retail replacement. This makes it a much more relevant starting point than a Morrowind-only engine, but FO3 campaign completeness remains a major gap.

The Android fork demonstrates an ARM64/Godot Android export path, but its own README and bring-up notes describe Android New Vegas work and explicitly say a complete Android campaign and device performance have not been verified. Do not treat that fork as a ready-made Fallout 3 Android runtime.

## Why this candidate

Fallout 3 and New Vegas share substantial Gamebryo-era data structures. OpenNV already has source-driven readers and gameplay/runtime work in that family. Reusing a compatible reader/runtime is more realistic than trying to execute `Fallout3.exe` directly or inventing a new engine from the Kotlin launcher.

OpenMW is not selected as the primary runtime: it is principally a Morrowind engine, and its newer-game support is described as early, basic walking-simulator functionality rather than a Fallout 3 campaign implementation.

## Required compatibility gates before integration

1. **Installation admission:** recognize the user's own Fallout 3 GOTY installation and DLC without modifying retail files.
2. **Data formats:** verify TES4 ESM/ESP records, BSA v104 archives, NIF meshes, DDS textures, KF animation, audio, lip-sync and movie paths against the user's installed build.
3. **Opening sequence:** load Vault 101 from a fresh game, preserve quest stages, dialogue conditions/results, NPC packages, doors, inventory, combat and scripted transitions.
4. **Persistence:** save, terminate the process, cold-continue, and verify cell/player/quest state.
5. **Android platform:** ARM64 Godot export, SAF-backed read-only game-data access, app-private writable saves/config/cache, input/touch mapping, audio, Vulkan/mobile rendering.
6. **Registry compatibility:** implement only the Windows registry API surface actually requested by the chosen runtime/game data path. A JSON profile in SharedPreferences is not a Windows registry implementation.
7. **Performance:** profile frame time, peak RSS/PSS, texture/archive cache, cell transition stalls, and 30–60 minute stability on the target phone.

## Integration boundary

Do not copy the whole OpenNevada Android repository into this Kotlin project. It is a separate Godot/C# product with its own toolchain and runtime. First pin an upstream commit and license, build its desktop/headless tests, and establish the Fallout 3 opening acceptance gate. Then choose one of two clean paths:

- **Preferred if the candidate passes:** make the Godot runtime the actual application entry point and package it for Android; keep Kotlin only for Android-specific SAF permission acquisition or replace the launcher entirely with a Godot-native picker.
- **Otherwise:** keep Project Capital as a launcher and implement a separately scoped native engine port, without pretending the current APK can run the retail executable.

## Current truth

The Project Capital APK still only selects and inspects an installation. It does not launch Fallout 3, does not execute Windows binaries, and does not yet provide a working Windows Registry API. No commercial game files are stored in this repository.
