# Trevor menu design reference

Reference inspected 2026-10-31: [official OneClient](https://polyfrost.org/projects/oneclient),
[Mods screenshot](https://polyfrost.org/ui.webp), [settings screenshot](https://polyfrost.org/settings.webp).
The official screenshots were downloaded and visually inspected. They establish layout,
not animation behavior. No OneClient code, logo, assets or dependency is used.

Observed: a substantial floating rounded dark window, fixed left rail, a search
field and category chips, large centered module artwork, colored label bands,
and a scrollable body. Detail pages preserve the shell and use horizontal rows.
The screenshots show blue accents and rounded sans typography. Exact source CSS
for the in-game menu is unavailable; dimensions below are Trevor implementation choices.

Trevor uses parchment text (#F3EBDD), muted slate (#A8B4C8), dark navy surfaces
(#111820 and #1D2632), lavender (#C4AFE9), sage (#A9C8AE) and brass (#E7C081).
The bundled OFL Nunito atlas preserves the established rounded typography.

The shell uses the available screen with 10–24 logical pixel side margins and
10–16 pixel vertical margins, capped at 1180 × 780 logical pixels. It is not
full screen. The rail appears where sufficient space exists. Header, body and
footer stay separate. Three roomy cards per row are preferred at ordinary sizes;
large screens can show four and small screens two. Cards use 16 pixel gutters,
up to 142 pixel height, a short description and a 30 pixel label band. Overflow
scrolls instead of shrinking the whole grid. Detail rows use 60 pixels plus gutters.

Opening fades actual layers over 260 ms; closing fades over 170 ms. The world
remains visible through the transition. Exponential hover, toggle and wheel
responses use elapsed time. Keyboard reveal and scrollbar dragging remain immediate.
Reduced motion bypasses transitions and scrolling interpolation. Optional artwork
depth retains slow continuous rotation; text and controls remain still.

Original icon pixels live in tools/menu-art.json and bake to one texture atlas.
Default icons use one texture quad each, replacing many rectangle submissions.
Glyph metrics are numeric and loaded once; hot text loops allocate no code-point arrays.
Help wrapping is cached per widget and width. No blur framebuffer, animation worker,
or closed-menu rendering is introduced. These reductions do not establish a claim
about whole-game startup time or the user's modpack performance.

The user selected a maximum of **three cards per row**, keeping the card browser.
A second module-navigation strip runs across the top on both browser and detail
pages. Readable tabs overflow horizontally with arrows and wheel input; selecting
a module keeps its tab in view. Tabs supplement the cards rather than replacing them.
