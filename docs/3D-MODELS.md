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

The five-stage scene is selected only for **Elusive + Extreme**, with rewards enabled and the
Extreme 3D toggle enabled. The serialized `JACKPOT` enum value is retained for existing config
compatibility; its visible label is now Extreme. Minimal, Classic and Extra preserve their
ordinary roll duration and presentation. The scene has a separate 5–14 second duration,
Trevor's entrance/wave/exit, a running rabbit with articulated legs/ears, distinct woodland,
desert, mushroom and End terrain, the native Ender Dragon approach, a single muted-aware growl,
and the gold reveal. It is a display scene and does not mutate the user's world.

The regular reel uses five 256px minted pelt emblems in one atlas, rounded typography and
rarity-specific beveled cards. Coins cost one texture quad each. Two horizontal gradient quads
replace 44 stepped edge-mask rectangles. `PeltCoinAtlas` reproduces the source art from the same
pure painter; it is baked before publication, rather than generated when a menu opens.

## Source references

- Fabric rendering model: https://docs.fabricmc.net/develop/rendering/basic-concepts
- PIP registry: https://maven.fabricmc.net/docs/fabric-api-0.161.0+26.2/net/fabricmc/fabric/api/client/rendering/v1/PictureInPictureRendererRegistry.html
- Trapper Crest face image: https://hypixelskyblock.minecraft.wiki/images/Trapper_Crest.png
- Image source page: https://hypixelskyblock.minecraft.wiki/w/Trapper_Crest

The 300×300 accessory image is used on the crest prism. Other solid meshes, the 2D module art,
and pelt-coin art are authored in this repository. The dragon model/texture come from the
installed Minecraft client. No video frames or assets from the user's reference clip are bundled.
