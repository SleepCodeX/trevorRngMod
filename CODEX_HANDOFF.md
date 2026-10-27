# Trevor Roll v1.3.1-dev | Codex handoff

**Repository:** https://github.com/SleepCodeX/trevorRngMod

**Working branch:** `fix/crest-hud-fabric-build`

**User-reported crash (2026-10-26 22:41:27):**

```text
Description: Initializing game
java.lang.RuntimeException: Could not execute entrypoint stage 'client' due to errors, provided by 'trevorroll' at 'dev.crestwatch.trapper.TrapperCrestHudClient'
Caused by: java.lang.IncompatibleClassChangeError:
Method 'net.fabricmc.loader.api.FabricLoader net.fabricmc.loader.api.FabricLoader.getInstance()' must be InterfaceMethodref constant
at dev.crestwatch.trapper.CrestHudConfig.<clinit>(CrestHudConfig.java:19)
at dev.crestwatch.trapper.TrapperCrestHudClient.onInitializeClient(TrapperCrestHudClient.java:37)
```

The earlier supplied 1.3.0 jar was compiled with inaccurate Fabric/Minecraft compatibility stubs, not the official dependencies. The original v1.1.1 project uses proper Fabric Loom. This crash is **not** the OSHI process-size NPE, which is a secondary crash-report generation warning.

**Immediate deliverable:** repair the integrated 1.3.x HUD code; build a genuine Minecraft 26.2 / Loader 0.19.5 / Fabric API 0.161.0+26.2 / JDK25 remapped JAR. Respect original v1.1.1 animations and commands and HUD state requirements from `AGENTS.md`.

**Source and patch:** ChatGPT produced the full `TrevorRoll_Codex_Handoff_v1.3.1-dev.zip` (real Java sources, build workflow, HUD, tests, docs) and baseline-apply patch `trevor-roll-v1.1.1-to-v1.3.1-codex.patch`. Have these attached to the Codex task or provided by the user, then extract/patch against `main`. They are not public GitHub assets.

**Validation checklist:**

1. Real Loom build: `./gradlew clean check remapJar --no-daemon`; never compile production classes against fake API stubs.
2. Confirm `javap -v` contains `InterfaceMethodref ... FabricLoader.getInstance` for the HUD config in the actual remapped JAR.
3. Ensure no `net/fabricmc/**` or `net/minecraft/**` stub classes are packaged.
4. Load the mod in a 26.2 Fabric client; test with/without other mods; test `/trevorroll test elusive` and `/trevorcrest demo uncommon`.
5. Confirm visual consistency, proper 100/500 thresholds, lore `Collected: 258 pelts`, + multiple hotbar crest priority, Rare warning clears immediately on removal.
6. Provide CI workflow run link and JAR artifact; create PR, do not merge until verified.

**Offline evidence:** On the 1.3.1 candidate pure-Java tests passed: 91,012 original roll assertions, 27 crest parsing/state, and 330 HUD layout/animation. These tests do **not** prove Fabric runtime compatibility.

**Release restriction:** The 1.3.0 experimental JAR must not be distributed as working.