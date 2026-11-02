package dev.trevorroll;


/**
 * Original hand-pixelled artwork, baked into one atlas; native texture quads
 * retain the same artwork on normal and affine depth faces.
 */
final class MenuArt {
    private MenuArt() {}

    // Original authored pixels are retained in tools/menu-art.json. The baked atlas
    // replaces hundreds of rectangle submissions and all runtime sprite packing.
    static void logo(MinecraftCanvas c, double x, double y, double scale) {
        c.menuSprite(0, 32, x, y, scale);
    }
    static void module(MinecraftCanvas c, int module, double x, double y, double scale) {
        c.menuSprite(Math.floorMod(module, 8) + 1, 24, x, y, scale);
    }

    /** The advanced path submits complete 3D meshes, never the 2D sprite atlas. */
    static void advanced(MinecraftCanvas c,int module,double x,double y,double scale,double angle) {
        c.model(module,x,y,scale,angle);
    }
}
