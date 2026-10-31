# Settings studio candidate (1.6.1-dev)

A visual-only refresh of the unified Trevor settings menu, based on the 1.6.0
release. Commands, configuration keys, HUD/reward animations, sounds and live
hunt/crest detection are unchanged.

- Fully opaque charcoal cards and chrome, with restrained lavender, gold and mint
  module accents, larger rounded icon wells and clearer text hierarchy.
- Persistent module sidebar where space allows; compact screens retain the
  Modules button and responsive dashboard. Incomplete card rows are centered.
- 52-pixel setting rows, 12-pixel gutters, equal column widths and stacked paired
  settings on narrow GUI scales. Help text is separated from labels and controls.
- Scrollable content is clipped independently of the fixed header/footer. Mouse
  input cannot activate a masked setting. Wheel, draggable scrollbar, Home/End,
  Page Up/Down and Tab/Shift+Tab all reach settings; keyboard focus scrolls into view.
- Native Minecraft widgets still provide the setting actions, slider behavior,
  keyboard activation and narration. No animation or command implementation changed.

## Validation

The real Java 25 focused source harnesses pass: 91,386 roll assertions, 68 crest
state/parser assertions, 313 crest visual/layout assertions and 1,233,699 Elusive
reward assertions. These focused checks are **not** a Loom production build.

Local Loom execution is blocked by this cloud sandbox denying the Unix-domain
socket capability probe. Sanctioned escalation failed during sandbox setup; no
stubbed dependencies, patched toolchain or manually assembled JAR were used.
The existing GitHub workflow runs the required real `clean build check` and
`runProductionClientGameTest` on the candidate branch, including bytecode checks.

Expanded real-client checks cover every setting and all seven modules at:

| Window | GUI scale | Purpose |
| --- | --- | --- |
| 854 × 480 | 1 | Wide sidebar and card grid |
| 1280 × 720 | 2 | Standard client-style sidebar |
| 1280 × 720 | 3 | Compact scrolling pages |
| 960 × 720 | 3 | Minimum 320 × 240 GUI |

They verify control preservation, no visible overlap, complete scroll/keyboard
reachability, clipping/input boundaries, repeated toggles, actual slider input
and disk persistence, and sound-picker Back versus Use behavior. Top/bottom
screenshots of each page are saved in the workflow's validation-evidence artifact.

This is a development candidate for review, not a merged or published release.
