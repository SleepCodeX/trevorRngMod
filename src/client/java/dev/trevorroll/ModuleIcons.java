package dev.trevorroll;

/** Original hand-pixelled module artwork; independent of OS emoji/font coverage. */
final class ModuleIcons {
    private ModuleIcons() {}
    static void draw(MinecraftCanvas c, int module, double x, double y, double scale, int ink) {
        // ink remains in the shared call signature; each illustration has its own
        // carefully balanced multitone palette rather than a flat one-colour tint.
        MenuArt.module(c, module, x, y, scale);
    }
}
