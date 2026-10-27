# Trevor Roll 1.3.1-dev

For **Minecraft 26.2**, **Fabric Loader 0.19.5**, **Fabric API 0.161.0+26.2** and **Java 25**. Download the attached `trevor-roll-1.3.1-dev.jar`, remove the older Trevor Roll JAR, and install only this version alongside Fabric API.

- Fixes the v1.3.0 Fabric startup crash by rebuilding every runtime class against real Fabric Loom dependencies. Both FabricLoader calls have verified interface bytecode; no compatibility stubs are bundled.
- Preserves the original Trevor animations, sounds, hunt detection and `/trevorroll` commands.
- Tracks the first actual Trapper Crest in hotbar slots 1-9; unrelated items and later completed crests cannot override it.
- Adds a bold gold 500-pelt switch alert identifying the active slot, plus a bold pink missing-hotbar reminder.
- Caches unchanged item lore and checks on client ticks instead of parsing during every rendered frame.
- Recognizes per-item 3/5/8 MP lore as a fallback to explicit perk/upgrade-stage evidence.

The attached JAR was built with real Fabric Loom and passed production bytecode checks and actual Minecraft client startup/in-world tests. The same source also passed those checks independently in GitHub Actions. Minecraft 26.2 is unobfuscated, so the correct Loom plugin's production task is `jar`, included by `build`; a remapping task is neither available nor required.

This is a **prerelease candidate**. Full validation in the intended Hypixel 26.2 modpack and third-party MP tooltip exposure remains pending. The PR remains unmerged and main is unchanged. Source, in-game screenshots and detailed validation are in [PR #1](https://github.com/SleepCodeX/trevorRngMod/pull/1).
