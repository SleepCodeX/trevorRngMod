# Solid models and the Extreme Elusive sequence

Advanced menu graphics use complete object meshes with positions, normals and material colors,
submitted through Fabric's `PictureInPictureRendererRegistry` and Minecraft's native Blaze3D
color/depth targets. Normal mode retains the original static pixel atlas. The machine, palette,
coin, gramophone, hide, keyboard and layout windows have their own geometry and silhouettes.
The palette has a real open thumb hole. The crest is a 24×24×15 beveled prism with an oblique
axis and four-second revolution; other menu objects revolve over ten seconds.

The Roll machine and Appearance palette remain the user-approved visual anchors; regression
signatures preserve their exact geometry and material colours. The other six objects use
new rounded cases, a curved hollow gramophone bell, shaped/stitched hide edges, deep keyboard
keycaps and broad stamped coin trim. [Art direction and viewed references](ART-DIRECTION.md)
record the restrained hand-painted fantasy inspiration.

Menu model targets render at twice their displayed resolution and use linear downsampling.
Premultiplied target blits preserve menu fades. Meshes are built lazily and cached. Small
cylinders use fewer segments; the broad stamped coin rim does not allocate geometry during rendering.
Fabric's renderer pool owns target instances and lifecycle. Scissor bounds are carried into
the extracted immutable state. Rendering uses Blaze3D, without direct OpenGL calls in production.

The Extreme scene is selected only for **Elusive + Extreme**, with rewards enabled and the
Extreme 3D intro toggle enabled. The serialized `JACKPOT` enum value remains compatible;
its visible label is Extreme. Minimal, Classic and Extra retain the ordinary roll duration.
The 5–14 second intro has three phases: the selected KayKit character enters and greets
under `beth notices you`; the native Ender Dragon approaches as the text switches to
`ELUSIVE MOB FOUND`; the dragon holds the final reveal. The native dragon growl is queued
once at that same transition, respecting the user's sound toggles and volume settings.
The display does not create entities or modify the world. No rabbit or coin appears
in the Extreme sequence.

The completed Extreme roll clears its ordinary result reel immediately. Its dragon reward
continues the scene clock and full opacity, then fades out. The large coin treatment uses an illustrated warm-gold palette, broad toon shading and a bold
edge, without metallic specular/reflection bands or fine milling. It belongs to Extra; Extreme's painter never submits a coin, including when its intro is off.

Rye supplies Western display titles; Outfit supplies captions throughout Elusive rewards.
Both use baked 64px glyph atlases with cached metrics and bundled OFL licenses. Run
`tools/bake-cinematic-fonts.py --title Rye-Regular.ttf --body Outfit.ttf` to reproduce them.
They load only when reward/cinematic text is first used; normal menu text is unchanged.

The regular reel uses five 256px minted pelt emblems in one atlas, rounded typography and
rarity-specific beveled cards. Coins cost one texture quad each. Two horizontal gradient quads
replace 44 stepped edge-mask rectangles. `PeltCoinAtlas` reproduces the source art from the same
pure painter; it is baked before publication, rather than generated when a menu opens.

## Source references

- Rye font: https://github.com/google/fonts/tree/main/ofl/rye
- Outfit font: https://github.com/google/fonts/tree/main/ofl/outfit
- Font licenses: `src/main/resources/assets/trevorroll/font-licenses/`
- Fabric rendering model: https://docs.fabricmc.net/develop/rendering/basic-concepts
- PIP registry: https://maven.fabricmc.net/docs/fabric-api-0.161.0+26.2/net/fabricmc/fabric/api/client/rendering/v1/PictureInPictureRendererRegistry.html
- Trapper Crest face image: https://hypixelskyblock.minecraft.wiki/images/Trapper_Crest.png
- Image source page: https://hypixelskyblock.minecraft.wiki/w/Trapper_Crest

The 300×300 accessory image is used on the crest prism. The module meshes, 2D module art and pelt-coin art are authored in this repository.
Beth uses the separately credited CC0 asset below. The dragon model/texture come from the
installed Minecraft client. No video frames or assets from the user's reference clip are bundled.


## Asset credits

**Kay Lousberg — KayKit Adventurers, Rogue (CC0).** The selected unhooded character,
authored gradient texture, cape and `Idle`, `Walking_A`, `Interact` clips come from the
[official free pack repository](https://github.com/KayKit-Game-Assets/KayKit-Character-Pack-Adventures-1.0),
commit `672074b73ba276876a19e8816ecdc5241817ab47`. The in-mod scene calls this character Beth.
The source export is Adventurers 1.0; the creator's current preview page shows the later
2.0 pack. We use the available official CC0 Rogue export, not the promotional picture.

- Source: `addons/kaykit_character_pack_adventures/Characters/gltf/Rogue.glb`
- Original GLB SHA-256: `e825437cd4d2ee9c1960b517a74a69101e33eb409ae7fa8cedc7134a998fbb7d`
- Original license, bundled unchanged: `assets/trevorroll/model-licenses/KayKit-CC0.txt`
- Creator: https://kaylousberg.itch.io/kaykit-adventurers
- Discovery included [this GitHub asset repository](https://github.com/KayKit-Game-Assets/KayKit-Character-Pack-Adventures-1.0)
  and [Reddit's free animation discussion](https://www.reddit.com/r/godot/comments/1jg4wtx/quaternius_just_dropped_a_high_quality_free_3d/).
  Reddit search excerpts were available; the full thread was unavailable to the retrieval service.

`python tools/bake-beth.py /path/to/Rogue.glb` reproduces the model and texture with numpy
and Pillow installed. The script checks the pinned source hash, removes optional weapons,
bakes the three skeleton clips at 60 Hz and converts coordinates/winding for Minecraft.
Only 3,567 vertices, 4,347 triangles and 42 joints are retained, in a 299,053-byte compressed
model and 15,886-byte texture. Original texture pixels and authored skin weights are preserved.

The client lazily decodes this bounded binary once, interpolates the baked poses at actual
render time and skins into reused arrays inside the ordered native render callback. It
loads no glTF parser or third-party animation engine. It does no per-frame mesh construction,
asset I/O or image processing. Clip blends keep entry, greeting and exit continuous.
The default 2D menu and lighter reward styles do not load the Beth model.
