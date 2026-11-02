# Solid models and the Extreme Elusive sequence

Advanced menu graphics use complete object meshes with positions, normals and material colors,
submitted through Fabric's `PictureInPictureRendererRegistry` and Minecraft's native Blaze3D
color/depth targets. Normal mode retains the original static pixel atlas. The machine, palette,
coin, gramophone, hide, keyboard and layout windows have their own geometry and silhouettes.
The palette has a real open thumb hole. The crest is a 24×24×15 beveled prism with an oblique
axis and four-second revolution; other menu objects revolve over ten seconds.

Menu model targets render at twice their displayed resolution and use linear downsampling.
Premultiplied target blits preserve menu fades. Meshes are built lazily and cached. Small
cylinders use fewer segments; the milled coin rim does not allocate geometry during rendering.
Fabric's renderer pool owns target instances and lifecycle. Scissor bounds are carried into
the extracted immutable state. Rendering uses Blaze3D, without direct OpenGL calls in production.

The Extreme scene is selected only for **Elusive + Extreme**, with rewards enabled and the
Extreme 3D intro toggle enabled. The serialized `JACKPOT` enum value remains compatible;
its visible label is Extreme. Minimal, Classic and Extra retain the ordinary roll duration.
The 5–14 second intro now has three phases: a sculpted cowboy hunter enters, tips his hat
and exits; the native Ender Dragon approaches; the dragon holds the reveal under **ELUSIVE**.
The display does not create entities or modify the world. Its native model, texture and
sound use the same Minecraft rendering path as 1.9.0.

The cowboy has a curved head and face, separate eyes/nose/ears/brows, curled wide-brim hat,
leather duster and vest, neckerchief, strap, buckle, rope loop, rounded sleeves/joints,
articulated forearms/gloved fingers and boots. There is no rabbit rig or token finale.
The completed Extreme roll clears its ordinary result reel immediately. Its dragon reward
continues the scene clock and full opacity, then fades out. The previous large coin treatment
belongs to Extra; Extreme's painter never submits a coin, including when its intro is off.

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

The 300×300 accessory image is used on the crest prism. Other solid meshes, the 2D module art,
and pelt-coin art are authored in this repository. The dragon model/texture come from the
installed Minecraft client. No video frames or assets from the user's reference clip are bundled.
