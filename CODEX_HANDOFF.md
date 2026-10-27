# Trevor Roll: Codex engineering handoff (2026-10-26)

> Historical input below. The actual 26.2 Loom build confirmed that this Minecraft version is unobfuscated and the correct plugin has no `remapJar` task. Use `./gradlew clean build check --no-daemon` and `./gradlew runProductionClientGameTest --no-daemon`. See [VALIDATION.md](VALIDATION.md) for current evidence. The original roll gains only a read-only visibility query so the crest card yields during a reveal; its animation, commands, sounds, and assets are preserved. The user's subsequent request replaces the old rarity priority: track the leftmost actual crest in slots 1-9, ignore other items, cache lore per item change, and display bold switch-at-500 and missing-hotbar actions. Later Rare crests must not override the active one.

## Mission

Fix the startup crash caused by the v1.3.0 Trapper Crest HUD and deliver a real, Fabric-Loom-built client-only JAR for Minecraft **26.2**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.2**, Java **25**. Preserve the original working v1.1.1 Trevor Roll animations, sounds, hunt detection, and `/trevorroll` commands. Target version is **1.3.1** only when proven working. This source snapshot is a **development candidate**, not a certified binary release.

- GitHub repository: https://github.com/SleepCodeX/trevorRngMod
- Target working branch: `fix/crest-hud-fabric-build`; leave `main` unchanged until CI and review.
- Stable reference: v1.1.1 files already in the repo; source snapshot here layers the 1.3.0 HUD on top.
- Prior broken build: `trevor-roll-1.3.0-fabric-0.19.5-mc26.2.jar`; **DO NOT reuse** its compiled HUD classes.

## Confirmed crash cause (not OSHI/system report warnings)

Real player's crash, 2026-10-26 22:41:27, description `Initializing game`:

```
java.lang.RuntimeException: Could not execute entrypoint stage 'client'
  provided by 'trevorroll' at 'dev.crestwatch.trapper.TrapperCrestHudClient'
Caused by: java.lang.IncompatibleClassChangeError:
  Method 'net.fabricmc.loader.api.FabricLoader net.fabricmc.loader.api.FabricLoader.getInstance()'
  must be InterfaceMethodref constant
  at dev.crestwatch.trapper.CrestHudConfig.<clinit>(CrestHudConfig.java:19)
  at dev.crestwatch.trapper.TrapperCrestHudClient.onInitializeClient(TrapperCrestHudClient.java:37)
```

Previous offline build used **fake/stubbed Minecraft/Fabric interfaces**. `javap -v` confirmed the broken class constant pool had:

```
Methodref net/fabricmc/loader/api/FabricLoader.getInstance
Methodref net/fabricmc/loader/api/FabricLoader.getConfigDir
```

FabricLoader is an **interface**, so the invocation must use an interface-method constant-pool reference. **A true rebuild with real Fabric dependencies is required.** This snapshot also resolves the config path lazily instead of initializing it during class loading, to reduce early initialization coupling. That change alone is not enough.

## What needs to work

### Stable original Trevor Roll

- Keep its existing hunt detection, reel animation and texture assets, sound effects, chat announcements, and rarity handling.
- Preserve `/trevorroll test <rarity>`, `toggle`, `sound`, `duration`, `skip`.
- Use the same `trevorroll` Fabric mod ID. Do not install both old and new JARs simultaneously.

### Crest HUD

- Render a compact polished HUD on the right, with bubbly lavender rounded borders, glow, paw icon, and smooth progress animation matching the original Trevor Roll's design.
- Scan **all nine hotbar slots** every HUD update; no manual selection required.
- Recognize an item called `Trapper Crest`, reading **item lore**, not counts guessed from server messages:
  - `Earn x1.1 Pelts from tracker quests.`, `Collect 100 Pelts to upgrade this accessory to UNCOMMON.`, `Collected: 0 pelts` -> Common stage and `0 / 100`.
  - `Earn x1.25 Pelts from tracker quests.`, `Collect 500 Pelts to upgrade this accessory to RARE.`, `Collected: 258 pelts` -> Uncommon, `Almost there!`, and `258 / 500`.
  - `Earn x1.5 Pelts from tracker quests.`, `Collected: 504 pelts`, `RARE ACCESSORY` -> `500 Pelts Gained! Switch Crests!`. The user intentionally holds multiple crests and swaps them upon upgrading.
- Perk tier (1.1x/1.25x/1.5x) is stronger evidence than the final accessory rarity label because recombobulation can change the label.
- If Rare crest leaves the hotbar, the Rare notification **must disappear immediately** (replace with the applicable Common/Uncommon state, or missing-crest state if none remain).
- If no crest is in the hotbar: `Get a COMMON Trapper Crest!`.
- Preserve optional `/trevorcrest demo missing|common|uncommon|rare`, `toggle`, `scale`, `top`, `right`, `reset` functionality. Validate that chat commands and HUD callbacks compile against the real Fabric 26.2 APIs.
- Item lore parsing must handle section-symbol formatting, commas, and negative/unknown values without manufacturing zero.

## Work plan

1. Compare the known-good repository `main` (v1.1.1) to this candidate source; preserve its original `dev/trevorroll` classes and assets.
2. First run `./gradlew clean check remapJar --no-daemon` under JDK 25. Resolve all actual Fabric 26.2 compilation issues, and never introduce compatibility stubs into the production runtime.
3. Confirm module entrypoints in `fabric.mod.json`; test `HudElementRegistry.attachElementBefore`, `GuiGraphicsExtractor`, `ItemLore`, and client commands against the real dependencies.
4. Inspect the remapped JAR's bytecode:

   ```bash
   JAR=$(find build/libs -maxdepth 1 -name 'trevor-roll-*.jar' ! -name '*-sources.jar' ! -name '*-dev.jar' | head -n 1)
   javap -verbose -classpath "$JAR" dev.crestwatch.trapper.CrestHudConfig | grep -E 'InterfaceMethodref.*FabricLoader\.(getInstance|getConfigDir)'
   jar tf "$JAR" | grep -E '^(net/fabricmc|net/minecraft)/' && echo 'ERROR: stub classes bundled' && exit 1 || true
   ```

   The `javap` output should contain correct interface method refs, not `Methodref` for `FabricLoader`.
5. Run unit/logic tests and add focused coverage for `Collected: 0/258/504 pelts`, stage transitions, removal of Rare crest, combinations of crests, and unknown-lore handling.
6. Run `./gradlew runClient` or the closest actual Fabric Minecraft 26.2 launch test. At minimum provide exact CI logs, then request a manual smoke test in the user’s modpack before public release.
7. Add a GitHub Actions workflow for JDK 25 Gradle + remapJar (the candidate workflow is included). Upload ONLY the remapped usable JAR to CI artifacts.
8. Open a PR from the fix branch, include build proof and screenshots. Merge to `main` and tag a release only after working validation.

## Build and release

```bash
./gradlew --version
./gradlew clean check remapJar --no-daemon
ls -lah build/libs
```

Current environment running this handoff **does not have Java 25 or official Fabric dependencies cached**, so a production JAR cannot be verified here. Don't claim successful in-game testing without actually doing it. **Do not ship the old 1.3.0 stub-compiled JAR.**

## Where to look

- `src/client/java/dev/crestwatch/trapper/CrestHudConfig.java`: lazy config-path resolution and prior crash point.
- `src/client/java/dev/crestwatch/trapper/TrapperCrestHudClient.java`: Fabric HUD callbacks, commands, item scanning.
- `src/client/java/dev/crestwatch/trapper/CrestHudCanvas.java`: Minecraft 26.2 rendering adapter, shares textures with original HUD.
- `src/main/java/dev/crestwatch/trapper/{CrestLoreParser,CrestHudState,CrestHudPainter,CrestHudAnimator}.java`: pure logic, stage detection, painter.
- `src/test/java/dev/crestwatch/trapper/`: runnable tests.
- `src/client/java/dev/trevorroll/TrevorRollClient.java`: reference for the working Fabric 26.2 rendering API and event registration.
- `.github/workflows/build.yml`: CI build and artifact.

## Acceptance result to report to user

Provide the exact `remapJar` build result (link to CI run), evidence Minecraft actually starts, exact `.jar` artifact URL or verified downloadable file, source branch/PR URL, and any remaining limitations. Never describe untested bytecode as verified.
