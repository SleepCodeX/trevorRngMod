# Trevor module browser (1.7.0)

The browser follows the [OneClient / OneConfig](https://polyfrost.org/projects/oneclient)
configuration workflow: a persistent utility sidebar, searchable category-filtered
module cards with centered artwork and accent label bands, then full-width settings
rows inside the same window. OneClient is a design reference, not a dependency;
all Trevor pixel artwork, menu code and depth effects are original.

## What's included

- Eight modules with opaque readable panels, generous gutters, aligned controls,
  responsive cards and a compact fallback at the minimum 320×240 GUI size.
- Custom paw/moon logo and eight multi-tone pixel illustrations. Lightweight,
  static artwork is the default.
- Optional **Advanced graphics** adds slowly rotating, obliquely projected icons:
  a thick square Trapper Crest prism, a custom pelt token and golden coin rims.
  Text and controls do not rotate. Hidden icons are not rendered; sprites are
  packed into rectangle runs once, with no animation threads or per-icon clocks.
- A time-based 220ms opening fade, with a separate reduced-motion toggle. The fade
  never blocks input or replays during module navigation.
- **Open menu key** is unbound by default. Configure it inline or in native
  Minecraft Controls; both use the same persisted mapping. Escape cancels capture,
  Delete/Backspace clears it, and a Reset button restores Unbound. The shortcut
  opens only during gameplay, never stealing chat or other screens.
- Existing hunt/crest logic, HUD/reward animations, audio layers, config keys and
  all commands are preserved. New preferences are additive.

## Validation

The local sandbox blocks Loom's Unix-domain socket capability probe. No stubs,
patched toolchain or handmade installable JAR are used. Focused source checks use
real Java25 and official dependencies; the GitHub workflow performs the required
real Loom `clean build check`, production bytecode verification and
`runProductionClientGameTest` against the packaged candidate.

The real-client suite covers every control at 854×480/scale1, 1280×720/scale2 and3,
and 960×720/scale3 (320×240 GUI). It checks preserved controls, scrolling and
keyboard reachability, masked input boundaries, no overlaps, filter/search empty
states, sidebar navigation, toggles/sliders and persistence, sound-picker cancel
and apply, unbound/bind/rebind/reset behavior, chat protection and repeat draining.
Separate deterministic tests check fade timing and frame-rate-independent rotation;
actual-client screenshots cover normal/advanced modes and edge-on prism/coin views.

The user authorized stable 1.7.0 after final validation. The main-only release
workflow publishes only after the real build and production-client checks pass.
