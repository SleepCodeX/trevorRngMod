package dev.trevorroll;

import java.util.ArrayList;
import java.util.List;

/**
 * Original, hand-pixelled field-journal artwork. Sprites are packed into rectangle
 * runs once, not rebuilt, sampled from an image, or randomized during rendering.
 */
final class MenuArt {
    private MenuArt() {}

    // Opaque, deliberately quiet colours: parchment, brass, lilac, moss and plum.
    private static int colour(char symbol) {
        return switch (symbol) {
            case 'o' -> 0xFF171B25; // outline
            case 'i' -> 0xFF292C3D; // inset
            case 'b' -> 0xFF736680; // lilac shade
            case 'a' -> 0xFFC6B0E6; // lilac
            case 'h' -> 0xFFF2E6C9; // parchment
            case 'y' -> 0xFFEBC981; // brass highlight
            case 'd' -> 0xFFA38154; // brass shade
            case 'g' -> 0xFF9DBEA7; // sage
            case 's' -> 0xFF526F65; // moss shade
            case 'r' -> 0xFFD6A491; // terracotta
            case 't' -> 0xFF876A59; // walnut
            default -> 0;
        };
    }

    private record Run(int x, int y, int width, int height, int colour) {}
    private record Sprite(int width, int height, Run[] runs) {}

    /** Equal horizontal runs in consecutive rows become one rectangle. */
    private static Sprite sprite(int width, int height, String... rows) {
        if (rows.length > height) throw new IllegalArgumentException("Pixel art height");
        List<Run> runs = new ArrayList<>();
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            if (row.length() > width) throw new IllegalArgumentException("Pixel art width: " + row);
            for (int x = 0; x < row.length();) {
                int start = x, ink = colour(row.charAt(x++));
                while (x < row.length() && colour(row.charAt(x)) == ink) x++;
                if (ink == 0) continue;
                int length = x - start, merge = -1;
                for (int n = runs.size() - 1; n >= 0; n--) {
                    Run r = runs.get(n);
                    if (r.y + r.height < y) continue;
                    if (r.x == start && r.width == length && r.colour == ink && r.y + r.height == y) {
                        merge = n; break;
                    }
                }
                if (merge < 0) runs.add(new Run(start, y, length, 1, ink));
                else {
                    Run r = runs.get(merge);
                    runs.set(merge, new Run(r.x, r.y, r.width, r.height + 1, ink));
                }
            }
        }
        return new Sprite(width, height, runs.toArray(Run[]::new));
    }

    private static final Sprite LOGO = sprite(32, 32,
        "................................",
        ".............yyyyyy.............",
        "..........yyyddddddyyy..........",
        ".......yyyddiiiiiiiiddyyy.......",
        ".....yyddiiiiiiiiiiiiiiddyy.....",
        "....yddiiiiiiiiiiiiiiiiiiddy....",
        "...yddiiiiiiiiiiiiiiihhhhiddy...",
        "...ydiiiiiiiiiiiiiiihhhiiiddy...",
        "...ydiiiaaiiiiaaiiihhhiiiiddy...",
        "...ydiiabbaiaabbaaihhiiiiiddy...",
        "...ydiiaaaaiaaaaaiihhiiiiiddy...",
        "...ydiiiabaiiabaaiihhhiiiiddy...",
        "...ydiaaiiiiiiiiaaiihhhhihddy...",
        "...ydabbaiiiiiiaabbiihhhhiddy...",
        "...ydaaaaiaaaaiaaaaiiiiihiddy...",
        "...ydiabiaaaaaaiabaiiiiiihddy...",
        "...ydiiiabaaaabaiiiiiiiiiddy...",
        "...ydiiabaaaaaabaiiiiiiiiiddy...",
        "...ydiiabaaaaaabaiiiiiiiiiddy...",
        "...yddiibbbabbbiiiiiiiiiiddy...",
        "....yddiiiiiiiiiiiiiiiiiddy....",
        "....yddiiiiiiiiiiiiiiiiddy.....",
        ".....yddiiiggggggggiiiddy......",
        "......yddiissssssssiiddy.......",
        ".......yddiiiiiiiiiddy........",
        "........yddiiiiiiiddy.........",
        ".........yddiiiiddy...........",
        "..........yddiiddy............",
        "...........yddddy.............",
        "............yyyy..............",
        "................................",
        "................................");

    private static final Sprite[] MODULES = {
        // Hunt ticket / three reel windows, with a sage animal in the middle.
        sprite(24, 24,
            "........................",
            ".....h............h.....",
            "....hyh..........hyh....",
            ".....h............h.....",
            "...dddddddddddddddddd...",
            "..dyhhhhhhhhhhhhhhhhyd..",
            "..dyddddddddddddddddyd..",
            "..dyoiiioiiiiiioiiioyd..",
            "..dyoaaioisgssioaaioyd..",
            "..dyoaaioiggggioaaioyd..",
            "..dyoaaioighhgioaaioyd..",
            "..dyoiiioigiggioiiioyd..",
            "..dyoaaioiggggioaaioyd..",
            "..dyoaaioissssioaaioyd..",
            "..dyoiiioiiiiiioiiioyd..",
            "..dyddddddddddddddddyd..",
            "..dyhhhhhhhhhhhhhhhhyd..",
            "...dddddddddddddddddd...",
            ".......tt......tt.......",
            ".......oo......oo......."),
        // Padded crest shield, parchment paw and a tiny brass clasp.
        sprite(24, 24,
            "..........yyyy..........",
            ".......yyyddddyyy.......",
            "....yyyddssssssddyyy....",
            "...yddssggggggggssddy...",
            "...ydsggggggggggggsdy...",
            "...ydsgghhgggghhggsdy...",
            "...ydsghhhhgghhhhgsdy...",
            "...ydsghhhhgghhhhgsdy...",
            "...ydsgghhgggghhggsdy...",
            "...ydshhgggggggghhsdy...",
            "...ydshhhgghhgghhhsdy...",
            "...ydshhhghhhhghhhsdy...",
            "...ydsghgghhhhgghgsdy...",
            "...ydsggghhhhhhgggsdy...",
            "...yddsgghhhhhhggsddy...",
            "....yddsggghhgggsddy....",
            ".....yddsggggggsddy.....",
            "......yddssssssddy......",
            ".......yddddddddy.......",
            "........yyyyyyyy........",
            "..........yyyy.........."),
        // Artist's palette: warm timber edge, lilac/sage/rose pigment wells.
        sprite(24, 24,
            "........................",
            ".........ddddddd........",
            "......dddhhhhhhdd.......",
            "....ddhhhhhhhhhhdd......",
            "...dhhhhaaahhhhhhhdd....",
            "..dhhhhaaaaahhhhhhhhd...",
            "..dhhhhaaaaahhhggghhd...",
            ".dhhhhhhaaahhhggggghhd..",
            ".dhhhhhhhhhhhhggggghhd..",
            ".dhhrrrhhhhhhhhggghhhd..",
            ".dhrrrrrhhhhhhhhhhhhhd..",
            ".dhrrrrrhhhhhyyhhhhohd..",
            ".dhhrrrhhhhhyyyyhhooood.",
            "..dhhhhhhhhhyyyyhooood..",
            "..dhhhhhhhhhhyyhoooood..",
            "...dhhhhhhhhhhhhdddd....",
            "....ddhhhhhhhddd........",
            "......ddddddd...........",
            "...........t............",
            "...........t............",
            "..........hhh..........."),
        // Two journal windows, with corner registration marks and sage content.
        sprite(24, 24,
            "........................",
            "..aaaa............aaaa..",
            "..a..................a..",
            "..a..bbbbbbbbbb......a..",
            ".....bhhhhhhhhb.........",
            ".....baaaaaaaab.........",
            ".....biiiiiiiib.........",
            ".....bisssssiiib........",
            ".....biggggiiib.........",
            ".....biiiiiiiib.........",
            ".....bbbbbbbbbbbbbbbb...",
            "...........byyyyyyyyb...",
            "...........bddddddddb...",
            "...........biiiiiiiib...",
            "...........biaggggiib...",
            "...........biaggggiib...",
            "...........biiiiiiiib...",
            "...........bbbbbbbbbb...",
            "..a..................a..",
            "..a..................a..",
            "..aaaa............aaaa.."),
        // Milled golden coin; the original paw motif is carved into its face.
        sprite(24, 24,
            "...................h....",
            "..................hyh...",
            ".......dddddddd....h....",
            ".....ddyyyyyyyydd.......",
            "....dyhhhhhhyyyyyd......",
            "...dyhhyyyyyyyyyyyd.....",
            "..dyhhyyyyyyyyyyyyyd....",
            "..dyhyyyyddyyddyyyyd....",
            ".dyhyyyyddddddddyyyyd...",
            ".dyhyyyyddddddddyyyyd...",
            ".dyhyyyyyddyyddyyyyyd...",
            ".dyhyyddyyyyyyyyddyd...",
            ".dyhyydddyyddyydddyd...",
            ".dyhyyddyyddddyyddyd...",
            ".dyhyyyyyddddddyyyyd...",
            "..dyyyyyyddddddyyyyd....",
            "..dyyyyyyyddddyyyyyd....",
            "...dyyyyyyyyyyyyyd.....",
            "....dyyyyyyyyyyyd......",
            ".....ddyyyyyydd........",
            ".......dddddd..........",
            "...h...................",
            "..hyh..................",
            "...h..................."),
        // Tiny gramophone: folded brass horn, walnut box, lilac spinning record.
        sprite(24, 24,
            "..................hh....",
            ".................hyh....",
            "...........dd...hhhyh...",
            "..........dyyddhhhhyd...",
            ".........dyhhyyyhyyd....",
            "........dyhhyyyyyyd.....",
            "........dyhyyyyyyd......",
            ".........dyyyyydd.......",
            "..........dddddt........",
            "...............t........",
            "...............t........",
            ".....bbbbbbbbbbt........",
            "....baaaaaaaaabt........",
            "....baahhhhaaabt........",
            ".....bbbbbbbbbbt........",
            "...tttttttttttttttt.....",
            "...thhhhhhhhhhhhhht.....",
            "...tdttttttttttttdt.....",
            "...tdtggggggggggtdt.....",
            "...tdtsssssssssstdt.....",
            "...tttttttttttttttt.....",
            "....oo..........oo......"),
        // Field progress: brass bar steps and a new sage sprout.
        sprite(24, 24,
            "........................",
            ".......ggg..............",
            "......ghhgg....ggg......",
            "......gghhgg..gghhg.....",
            ".......gghhgggghhg......",
            "........ggggggggg.......",
            "...........ss...........",
            "...........ss.....yy....",
            "...........ss.....yd....",
            "...........ss.....yd....",
            "...........ss.yyy.yd....",
            "...........ss.yyd.yd....",
            "...........ss.yyd.yd....",
            ".........yyyssyyd.yd....",
            ".........yydssyyd.yd....",
            ".....yyy.yydssyyd.yd....",
            ".....yyd.yydssyyd.yd....",
            ".....yyd.yydssyyd.yd....",
            "...tttttttttttttttttt...",
            "...ssssssssssssssssss...",
            ".....ss...ss....ss......"),
        // Personal controls: a little lilac keyboard and a guiding brass star.
        sprite(24, 24,
            "..................h.....",
            ".................hyh....",
            "..................h.....",
            "........................",
            "...bbbbbbbbbbbbbbbbbb...",
            "..bahhhhhhhhhhhhhhhhab..",
            "..baiiiiiiiiiiiiiiiiab..",
            "..baiagagagagagagagiab..",
            "..baiiiiiiiiiiiiiiiiab..",
            "..baiagagagagagagagiab..",
            "..baiiiiiiiiiiiiiiiiab..",
            "..baiagagagagagagagiab..",
            "..baiiiiiiiiiiiiiiiiab..",
            "..baiyaaaaaaaayiaaaiab..",
            "..baiiiiiiiiiiiiiiiiab..",
            "..baaaaaaaaaaaaaaaaaab..",
            "...bbbbbbbbbbbbbbbbbb...",
            "....oooo........oooo....")
    };

    private static void draw(MinecraftCanvas c, Sprite sprite, double x, double y, double scale) {
        c.push(x - sprite.width * scale / 2, y - sprite.height * scale / 2, scale);
        try {
            for (Run r : sprite.runs) c.pixelRect(r.x, r.y, r.width, r.height, r.colour);
        } finally { c.pop(); }
    }

    /** 32 × 32 original paw-and-moon crest, centred at x/y. */
    static void logo(MinecraftCanvas c, double x, double y, double scale) { draw(c, LOGO, x, y, scale); }

    /** 24 × 24 original module sprite, centred at x/y. */
    static void module(MinecraftCanvas c, int module, double x, double y, double scale) {
        draw(c, MODULES[Math.floorMod(module, MODULES.length)], x, y, scale);
    }

    /**
     * Optional orthographic depth treatment. The caller supplies one shared slow
     * angle, so there are no clocks or animation objects per icon. Hidden icons
     * must not call this method. Plate faces use the same cached pixel artwork.
     */
    static void advanced(MinecraftCanvas c, int module, double x, double y, double scale, double angle) {
        int index = Math.floorMod(module, MODULES.length);
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
                draw(c, MODULES[index], 0, 0, .9);
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
            } else draw(c, MODULES[4], 0, 0, 1);
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
