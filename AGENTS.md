# Codex instructions for Trevor Roll

## Environment

- Repository: `SleepCodeX/trevorRngMod`.
- Safe working branch: `fix/crest-hud-fabric-build` (create a new feature branch if necessary). Never commit an unverified build directly to `main`.
- Minecraft Java Edition **26.2**; Fabric Loader **0.19.5**; Fabric API **0.161.0+26.2**; Java **25**.
- Build with the **real Fabric Loom Gradle toolchain** via the Gradle wrapper (`./gradlew clean build check --no-daemon`). Minecraft 26.2 is unobfuscated: the correct `net.fabricmc.fabric-loom` plugin produces the installable artifact with `jar` (included by `build`), and has no `remapJar` task. Do not switch to remapping Loom or invent a remapping task. Do not use Java compatibility stubs or assemble a JAR by hand.

## User goal

Keep every working behavior and `/trevorroll` command from 1.1.1 and integrate a polished, bubbly Trapper Crest HUD, matching the original roll's colors, paw icon, circles, and glows. The user's latest request overrides the historical rarity priority: scan slots 1-9 and track the first actual Trapper Crest from the left. Ignore non-crest items and later crests, even if a later crest is completed. Read actual lore (`Collected: <number> pelts`), show common progress to 100 and uncommon progress to 500. At 500 or more pelts (or an active Rare crest), show a bold `SWITCH CRESTS!` action identifying the active slot. If none is detected, show a bold `MOVE TRAPPER CREST TO YOUR HOTBAR!` side reminder. Clear/update warnings within the next client tick after item removal or lore changes. Cache unchanged item components and parse outside rendering. Preserve `/trevorcrest` controls.

## Known crash (from real user crash report)

The user also requested per-item MP detection: recognize 3/5/8 MP lore as Common/Uncommon/Rare when explicit perk/upgrade-stage evidence is absent. Do not confuse total player MP with item MP, or override recombobulation-safe perk evidence.

```
java.lang.RuntimeException: Could not execute entrypoint stage 'client'
provided by 'trevorroll' at 'dev.crestwatch.trapper.TrapperCrestHudClient'
Caused by: java.lang.IncompatibleClassChangeError:
 Method 'net.fabricmc.loader.api.FabricLoader net.fabricmc.loader.api.FabricLoader.getInstance()'
 must be InterfaceMethodref constant
 at dev.crestwatch.trapper.CrestHudConfig.<clinit>(CrestHudConfig.java:19)
```

The 1.3.0 JAR was built using **inaccurate test/compatibility stubs**; FabricLoader was encoded as `Methodref` instead of `InterfaceMethodref`. **Recompile all client classes with real dependencies**. Moving the config path out of static initialization is only a defensive change and not a substitute for this.

## Definition of done

1. `./gradlew clean build check --no-daemon` succeeds with Java 25, including tests and `verifyProductionJar`.
2. Inspect bytecode with `javap -p -v` on the **production JAR**: `FabricLoader.getInstance` and `getConfigDir` resolve via the correct interface constant-pool references. No `net/fabricmc/**` or `net/minecraft/**` stub class files are bundled in the mod.
3. Vanilla Minecraft 26.2 + Loader 0.19.5 + Fabric API starts with the mod; reproduce startup with the user's full modpack if possible.
4. `/trevorroll test elusive` displays the original roll; `/trevorcrest demo common`, `uncommon`, `rare`, `missing` work. Hide/show and config commands work.
5. Inspect HUD at multiple GUI scales/resolutions; all texts fit; progress accurate, rare alert disappears upon removing crest, stage determined by 1.1x/1.25x/1.5x perk even if recombobulated.
6. Publish a JAR only after a real Loom production build and Minecraft launch smoke test (`./gradlew runProductionClientGameTest --no-daemon`). Keep the development version until modpack validation and review; do not merge or tag a release automatically.

See `CODEX_HANDOFF.md` for a detailed plan and failure evidence. Do not recreate or replace the original Trevor Roll source unnecessarily.

## Current release approval

On 2026-10-27, the user confirmed that the refined 1.4.0-dev mod works well and explicitly requested promotion to a stable release and marking it as the latest release. This authorizes merging the settings/HUD studio PR, publishing 1.4.0 and marking it latest after the real Loom build and production-client checks pass. Runtime behavior must remain unchanged during this version-only promotion. The earlier stable 1.3.1 promotion was also user-approved.


## Elusive animation update authorization

The user requested four selectable Elusive-only reward animations and explicitly instructed merging them into main and making a JAR. Merge the feature after the real Loom build, bytecode checks and production-client tests pass. The user subsequently explicitly requested merging and releasing this update. Publish stable 1.5.0 and mark it Latest after the real build and production-client checks pass.


## 1.6.0 authorization
The user requested a complete release of the HD coin, settings redesign, two-layer audio and compact pelts/hour tracker, explicitly excluding elapsed/last/average crest timings. Publish 1.6.0 after the real Loom and production-client checks pass.
