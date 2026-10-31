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

    /**
     * Optional orthographic depth treatment. The caller supplies one shared slow
     * angle, so there are no clocks or animation objects per icon. Hidden icons
     * must not call this method. Plate faces use the same cached pixel artwork.
     */
    static void advanced(MinecraftCanvas c, int module, double x, double y, double scale, double angle) {
        int index = Math.floorMod(module, 8);
        double cs = Math.cos(angle), sn = Math.sin(angle);
        double pitchSin = .34, pitchCos = .940425435;
        double xx = cs * scale, xy = sn * pitchSin * scale;
        double yy = pitchCos * scale, zx = sn * scale, zy = -cs * pitchSin * scale;
        double depth = index == 1 ? 5 : 3.5;
        double front = cs >= 0 ? -depth / 2 : depth / 2;
        boolean coin = index == 4 || index == 6;
        if (coin) {
            // Nine shallow, layered discs expose a milled, genuinely thick edge.
            for (int step = 8; step >= 0; step--) {
                double z = front + (cs >= 0 ? 1 : -1) * depth * step / 8;
                c.affine(x + z*zx, y + z*zy, xx, xy, 0, yy);
                try { coinDisc(c, step % 2 == 0 ? 0xFF947346 : 0xFFBC975C); }
                finally { c.pop(); }
            }
            // At a perfectly edge-on angle, the transformed faces have no area.
            // A slim side plane still shows the coin's thickness without flicker.
            if (Math.abs(cs) < .08) {
                c.affine(x - depth*zx/2, y - 9*yy - depth*zy/2, zx, zy, 0, yy);
                try { c.pixelRect(0, 0, (int)Math.ceil(depth), 18, 0xFFB18C54); }
                finally { c.pop(); }
            }
        } else {
            // Visible side and top of a square prism. The Crest is intentionally
            // thicker, like a small enamel accessory rather than a flat card.
            double side = sn >= 0 ? 12 : -12;
            c.affine(x + side*xx - depth*zx/2, y + side*xy - 12*yy - depth*zy/2,
                zx, zy, 0, yy);
            try { c.round(0, 0, depth, 24, 0, index == 1 ? 0xFF8E7656 : 0xFF5B5369); }
            finally { c.pop(); }
            c.affine(x - 12*xx - depth*zx/2, y - 12*xy - 12*yy - depth*zy/2,
                xx, xy, zx, zy);
            try { c.round(0, 0, 24, depth, 0, index == 1 ? 0xFFD7BD8B : 0xFF9F8AB8); }
            finally { c.pop(); }
        }
        if (Math.abs(cs) < .025) return;
        double direction = cs >= 0 ? 1 : -1;
        c.affine(x + front*zx, y + front*zy, xx*direction, xy*direction, 0, yy);
        try {
            if (!coin) {
                c.pixelRect(-12, -12, 24, 24, index == 1 ? 0xFFD1B886 : 0xFFB3A0CA);
                c.pixelRect(-11, -11, 22, 22, 0xFF302E3D);
                module(c, index, 0, 0, .9);
            } else if (index == 6) {
                // A bespoke pelt-token face: rolled hide, not a generic currency sign.
                coinDisc(c, 0xFFE5BF72);
                c.pixelRect(-7, -8, 14, 1, 0xFFF7DE9E);
                c.pixelRect(-5, -5, 3, 3, 0xFF8D634B);
                c.pixelRect(2, -5, 3, 3, 0xFF8D634B);
                c.pixelRect(-3, -3, 6, 9, 0xFFB48158);
                c.pixelRect(-5, -1, 10, 5, 0xFFB48158);
                c.pixelRect(-2, -2, 2, 6, 0xFFD5A26C);
                c.pixelRect(-5, 5, 3, 3, 0xFF8D634B);
                c.pixelRect(2, 5, 3, 3, 0xFF8D634B);
            } else module(c, 4, 0, 0, 1);
        } finally { c.pop(); }
    }

    private static void coinDisc(MinecraftCanvas c, int ink) {
        c.pixelRect(-5, -11, 10, 2, ink);
        c.pixelRect(-8, -9, 16, 3, ink);
        c.pixelRect(-10, -6, 20, 12, ink);
        c.pixelRect(-8, 6, 16, 3, ink);
        c.pixelRect(-5, 9, 10, 2, ink);
    }

}
