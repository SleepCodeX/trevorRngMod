# Trevor Roll development and reference

Build instructions, complete commands and implementation details for the current source. For a short introduction and installation, see the [main README](../README.md).

## Requirements

| Component | Version |
| --- | --- |
| Minecraft Java Edition | 26.2 |
| Fabric Loader | 0.19.5 or newer; built against 0.19.5 |
| Fabric API | 0.161.0+26.2 or a newer compatible 26.2 build |
| Java / JDK for building | 25 |
| Current release | 1.8.0 |

## Build

Install **JDK 25**, set `JAVA_HOME` to that JDK, and open a terminal in the folder containing `build.gradle`.

Windows PowerShell:

```powershell
.\gradlew.bat clean build check --no-daemon
```

macOS / Linux:

```sh
bash gradlew clean build check --no-daemon
```

The included Gradle wrapper downloads Gradle and the required dependencies on the first build. There is no need to install Gradle separately.

Build output:

```text
build/libs/trevor-roll-1.8.0.jar          # Installable production JAR
build/libs/trevor-roll-1.8.0-sources.jar  # Java source archive
```

For a development Minecraft instance, run `gradlew.bat runClient` on Windows or `bash gradlew runClient` on macOS/Linux.

Minecraft 26.2 uses unobfuscated names. The official `net.fabricmc.fabric-loom` plugin's `jar` task produces the production JAR; there is no `remapJar` task for this target. `check` verifies the actual JAR with `javap`, including both `FabricLoader` interface references and the absence of bundled runtime stubs. Run `gradlew.bat runProductionClientGameTest --no-daemon` (or `bash gradlew runProductionClientGameTest --no-daemon`) to launch the packaged mod in a disposable world and check commands, hotbar lore and HUD screenshots. The separate `-gametest.jar` is a test harness and must not be installed by users.

## Commands

All commands run locally on the client.

| Command | Effect |
| --- | --- |
| **`/trevormod settings`** | **Open the module browser and all mod settings** |
| `/trevormod preview` | Preview the selected Elusive celebration |
| `/trevormod trackerStart` | Start or reset the pelts/hour session |
| `/trevormod trackerStop` | Stop tracking pelts/hour |
| `/trevormod trackerStats` | Print the current pelts/hour |
| `/trevorroll config` / `/trevorcrest config` | Open the same settings menu (legacy aliases) |
| `/trevorroll` | Show help |
| `/trevorroll test` | Preview a random rarity |
| `/trevorroll test elusive` | Preview a chosen rarity |
| `/trevorroll toggle` | Enable or disable the mod |
| `/trevorroll sound` | Toggle roll sounds |
| `/trevorroll duration 3` | Set the roll duration, from 1 to 8 seconds |
| `/trevorroll skip` | Reveal the pending hunt immediately |

Chosen preview rarities: `trackable`, `untrackable`, `undetected`, `endangered`, `elusive`.

Roll and menu preferences are saved in the instance's `config/trevor-roll.json`. Crest preferences use `config/trapper-crest-hud.properties`. The menu shortcut uses Minecraft's native keybinding storage, so changes made in the mod menu and Minecraft Controls stay in sync.

## Behavior

The mod matches Trevor's assignment format, for example:

```text
[NPC] Trevor: You can find your UNDETECTED animal near the Overgrown Mushroom Cave.
```

It temporarily hides that assignment from vanilla chat, plays the reveal, and displays:

```text
UNDETECTED
Go to Overgrown Mushroom Cave to kill it now!
```

- The winning rarity is always the original server assignment. Decorative reel cards do not affect the hunt.
- Live interception runs only for `hypixel.net` and its subdomains; preview commands also work in singleplayer.
- Unknown assignment formats and Trevor's failure/cooldown messages remain visible.
- Other mods may still read the original message or show their own hunt overlays.
- You can move during the reveal. The hunt timer continues running.
- Pressing F1 hides the panel with the normal HUD; the result still appears in chat after the roll.
- The result panel remains visible for 4.5 seconds after the roll ends.

## Trapper Crest HUD

Trevor Roll includes a second Fabric client initializer, `dev.crestwatch.trapper.TrapperCrestHudClient`, inside **the same mod JAR**. Only one Trevor Roll JAR is needed. The HUD is client-only and doesn't send commands or modify player items.

It scans **hotbar slots 1–9**, not your whole inventory. A Trapper Crest can be anywhere in those slots, even when not currently selected. It reads the server-provided tooltip lore text (not a locally estimated running total): e.g. `Collected: 258 pelts`. A crest in your Accessories Bag does not count as being in your hotbar.

| Stage in hotbar | Display |
| --- | --- |
| No crest | Bold pink `MOVE TRAPPER CREST / TO YOUR HOTBAR!` reminder |
| Common crest (`x1.1`) | Live `Collected / 100 Pelts` and progress bar |
| Uncommon crest (`x1.25`) | `Almost there!` and live `Collected / 500 Pelts` progress bar |
| Active crest reaches 500 pelts, or has the Rare perk (`x1.5`) | Bold gold `500 PELTS REACHED / SWITCH CRESTS!`, with the slot to remove |
| Active crest leaves the hotbar | Track the next crest from the left within the next client tick, or show the missing-crest reminder. |

The HUD tracks the **first Trapper Crest from the left, scanning slots 1 through 9**. Other items are ignored. A completed crest farther right cannot override the crest currently gaining pelts. The panel labels the active slot; at 500 pelts it tells you which crest to remove. Detecting the stage from the lore's `x1.1`, `x1.25`, or `x1.5` perk also handles recombobulated crests without mistaking display rarity for upgrade stage. If no Pelt line is exposed by the server, the counter displays `--` instead of a fabricated zero.

When the item's lore includes an MP value, **3 MP = Common, 5 MP = Uncommon, 8 MP = Rare** is also recognized. Explicit perk and upgrade-progress text takes precedence over MP to handle recombobulated accessories. This reads per-item lore, never the player's total MP, and continues to use the actual pelt count for progress.

The HUD appears by default only on Hypixel, near the upper-right corner. It uses Trevor Roll's rounded shell, paw icon and soft textures. Completion and missing-crest states have a thicker colored border, stronger glow, larger action text, and a slow breathing highlight. The crest card yields during the original reel animation. Hotbar checks run once per client tick; unchanged item components reuse cached lore and state, so rendering does not repeatedly scan items or parse text. Warnings update on the next tick after a swap.

![Trapper Crest HUD captured in Minecraft](media/1.6.0/crest-progress.png)

### Crest HUD commands

| Command | Effect |
| --- | --- |
| `/trevorcrest` | Show crest HUD help |
| `/trevorcrest toggle` | Show/hide only the crest HUD |
| `/trevorcrest demo common` | Preview Common state for 14 seconds |
| `/trevorcrest demo uncommon` | Preview Uncommon progress (258/500) |
| `/trevorcrest demo rare` | Preview Rare completion (504 collected) |
| `/trevorcrest demo missing` | Preview no-crest reminder |
| `/trevorcrest scale 1.15` | Change panel size (0.65 through 1.60) |
| `/trevorcrest top 120` | Move down (GUI pixels) |
| `/trevorcrest right 18` | Set distance from right edge |
| `/trevorcrest reset` | Restore positioning, sizing and visibility defaults |

Commands take effect immediately. The crest card temporarily yields while the original Trevor reveal is active, keeping its animation unobstructed at large GUI scales.

Or customize the auto-created `config/trapper-crest-hud.properties` file and restart Minecraft:

```properties
enabled=true
onlyHypixel=true
top=85
rightMargin=12
scale=1.0
```

**Common and Uncommon upgrade thresholds** match the provided October 2026 screenshots: Common to Uncommon at **100** Pelt progress, and Uncommon to Rare at **500**. This HUD tracks each individual crest's accumulated Pelts, not your purse or total player lifetime Pelts.

## Source layout

```text
src/main/java/dev/trevorroll/
  Hunt.java                Assignment parser and rarity definitions
  Reel.java                Reel timing and the server-assigned winning card
  HudPainter.java          Shared UI layout and drawing logic
  MenuMotion.java          Frame-independent menu fade and icon rotation
src/client/java/dev/trevorroll/
  TrevorRollClient.java     Fabric events, chat filtering, commands and sounds
  MinecraftCanvas.java      Minecraft GUI drawing adapter
  RollConfig.java           Local settings
  TrevorSettingsScreen.java  Module browser, search and settings pages
  MenuKeybind.java           Native unbound-by-default menu shortcut
  MenuArt.java               Original pixel icons and optional projected depth
src/main/java/dev/crestwatch/trapper/
  CrestLoreParser.java     Reads Pelt totals and upgrade stage from item lore
  CrestHudState.java       Derives missing/common/uncommon/rare HUD state
  CrestHudFrame.java       Animation frame data (pure Java)
  CrestHudAnimator.java    Progress easing + short spring on stage changes
  CrestHudPainter.java     Floating bubble card layout, testable offline
src/client/java/dev/crestwatch/trapper/
  TrapperCrestHudClient.java  Hotbar scan, commands, renderer registration
  CrestHudCanvas.java      Adapter using Trevor Roll's circle + glow textures
  CrestHudConfig.java      HUD preferences
src/test/java/dev/crestwatch/trapper/
  CrestHudStateTest.java   Validates screenshot lore and state changes
  CrestHudAnimationTest.java  Visual layout and easing checks
src/main/resources/
  fabric.mod.json           Mod metadata and required versions
  assets/trevorroll/        Rounded-corner and glow textures
src/test/java/dev/trevorroll/
  LogicTest.java            Executable parser and reel checks
```

Versions are configured in `gradle.properties`. Mod compatibility requirements are declared in `src/main/resources/fabric.mod.json`.

## Validation

`build` runs the Trevor parser/reel tests, the Trapper Crest parsing/state tests, and the layout/animation checks (including the screenshot lore and Rare crest removal). The Trevor checks: all five rarities, messages that must stay visible, the supplied assignment format, and landing/timing behavior over 500 seeded rolls. You can run these checks separately with `gradlew.bat logicTest` or `bash gradlew logicTest`.

All runtime classes are compiled from source by Fabric Loom against the official dependencies. No classes are reused from the broken 1.3.0 binary. See [VALIDATION.md](../VALIDATION.md) for build, bytecode and real-client test evidence and the user-confirmed in-game validation of earlier releases. GitHub Actions repeats the build and production-client tests and uploads the installable mod JAR, with reports and screenshots in a separate evidence artifact. Candidate validation also covers module search and filters, small GUI scales, clipped input, keyboard navigation, keybind persistence, reduced motion and advanced graphics. A successful candidate build is not a release.

