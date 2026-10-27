# Trevor Roll: Codex working instructions

Work on `fix/crest-hud-fabric-build`, not `main`. Minecraft **26.2**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.2**, Java **25**.

**Known breakage:** Trevor Roll 1.3.0's client entrypoint `dev.crestwatch.trapper.TrapperCrestHudClient` crashes immediately in `CrestHudConfig.<clinit>` with `java.lang.IncompatibleClassChangeError: FabricLoader.getInstance() must be InterfaceMethodref constant`. The experimental jar was compiled with fake Fabric interfaces. **You must rebuild the HUD using real Fabric Loom dependencies and remapJar.** Do not reuse the broken JAR or make compatibility stubs.

Preserve all original v1.1.1 Trevor animation/commands (`/trevorroll`). Add a polished right-side bubbly Trapper Crest HUD that checks all nine hotbar slots, reads lore `Collected: N pelts`, recognizes Common (x1.1, 100 Pelts), Uncommon (x1.25, 500 Pelts, 'Almost there!'), Rare (x1.5, '500 Pelts Gained! Switch Crests!'), and clears the rare warning as soon as that crest leaves the hotbar. Keep `/trevorcrest` demo/position/scale controls. Match the original glowing rounded style and avoid HUD overlap.

The user has a Codex handoff source ZIP `TrevorRoll_Codex_Handoff_v1.3.1-dev.zip` and Git patch `trevor-roll-v1.1.1-to-v1.3.1-codex.patch` from ChatGPT; the repo's main branch still contains the stable original and this branch initially has handoff documents only. Ask them to attach the ZIP if not in your task context, or implement the feature directly from this description. The ZIP includes a full candidate source and tests.

Required: `./gradlew clean check remapJar --no-daemon` with JDK25. Inspect actual remapped JAR bytecode via `javap -v`: FabricLoader.getInstance must be `InterfaceMethodref` and no stub `net/fabricmc` or `net/minecraft` classes can be bundled. Launch Minecraft with the new JAR and Fabric API. Do not label as release-ready until actual startup succeeds. Raise a PR for review; do not merge a broken version.