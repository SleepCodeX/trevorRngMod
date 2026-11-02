# Trevor module browser (1.9.0)

The browser follows the [OneClient / OneConfig](https://polyfrost.org/projects/oneclient)
configuration workflow: a persistent utility sidebar, searchable category-filtered
module cards with centered artwork and accent label bands, then full-width settings
rows inside the same window. Trevor's palette, code and module artwork are its own.

## What's included

- Eight modules with opaque readable panels, generous gutters, aligned controls,
  at most three cards per row, quick top tabs and a compact fallback at 320×240 GUI size.
- A paw/moon logo and eight pixel illustrations in a baked atlas. Static 2D art is the default.
- **Advanced graphics** switches to eight native lit 3D object models, with cached geometry,
  true depth, object-specific silhouettes and 2× rendering with linear downsampling.
  The machine has reels and a lever; the palette has a real thumb hole and raised paint wells.
  The crest is a 24×24×15 prism, tilted 32° with the accessory image on both faces.
  Crest rotation takes four seconds; other objects take ten. Labels and controls remain still.
- Opening fades last 260 ms; closing fades last 170 ms. Hover, toggles and wheel scrolling
  use elapsed-time easing. Reduced motion makes these immediate. Module navigation does
  not replay the opening fade. Hidden icons are skipped; no animation thread runs when closed.
- **Open menu key** starts unbound. Inline capture and native Minecraft Controls use the same
  persisted mapping. Escape cancels, Delete/Backspace clears, Reset restores Unbound.
  The shortcut opens only during gameplay and never steals chat or other screens.
- Jackpots includes the **Extreme 3D roll** toggle and its independent duration. The cinematic
  requires an Elusive assignment, Extreme style, rewards enabled and that toggle enabled.
  Minimal, Classic and Extra retain the regular reel. Existing configuration values persist.

## Validation

Real Java 25 Loom `clean build check` verifies logic, animation policy, geometry and production
bytecode. `runProductionClientGameTest` launches the installable JAR in Minecraft 26.2.
The local environment and GitHub workflow both support native production-client tests.
No compatibility stubs, patched toolchain or handmade installable JAR are used.

The client suite covers every control at 854×480/scale 1, 1280×720/scales 2 and 3,
and 960×720/scale 3. It checks scrolling, keyboard reachability, clipped input, row bounds,
search and categories, native sliders, persistence, sound selection, unbound/bind/rebind/reset,
chat protection, repeated-key draining, reduced motion and clean animated closing.
GPU captures cover all eight solid models at front, oblique, edge-on and rear angles,
the actual rotating dashboard, five cinematic chapters, rabbit poses and every rarity card.
Live commands verify that only Extreme Elusive uses the native cinematic and its duration.

See [VALIDATION.md](../VALIDATION.md) for exact source/run evidence and
[3D-MODELS.md](3D-MODELS.md) for rendering and asset sources. The main release workflow
publishes only after a successful real build and production-client run.
