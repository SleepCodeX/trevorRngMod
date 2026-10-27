package dev.crestwatch.trapper;

import java.util.Locale;

/**
 * Trevor Roll-inspired floating card, rendered with the SAME soft circle/glow textures
 * and paw motif as the original rarity wheel. Pure Java to allow offline visual tests.
 */
public final class CrestHudPainter {
    private CrestHudPainter() {}
    public static final double WIDTH = 228;
    public static final double HEIGHT = 118;
    private static final int PANEL = 0xFF18182C;
    private static final int INK = 0xFFF6F2FF;
    private static final int MUTED = 0xFFAAA9C5;
    private static final int[] ACCENT = {
        0xFFFF8FA3, // missing: clear action color
        0xFFB9BEC9, // common: neutral gray from the initial crest tier
        0xFF92E3BA, // uncommon
        0xFFFFD06E  // 500 pelts: bright switch alert
    };

    /** An adapter for either Minecraft GUI rendering or preview/offline testing. */
    public interface Canvas {
        void push(double x, double y, double scale);
        void pop();
        void round(double x, double y, double width, double height, double radius, int argb);
        void ellipse(double x, double y, double width, double height, int argb);
        void glow(double x, double y, double width, double height, int argb);
        void text(String text, double x, double y, double size, int argb);
        double textWidth(String text);
    }

    /** One logical pixel in this renderer is one scaled Minecraft GUI pixel. */
    public static void draw(Canvas c, CrestHudState state, CrestHudFrame frame,
                            int guiWidth, int guiHeight, int top, int right, double userScale) {
        if (guiWidth < 132 || guiHeight < 108) return;
        double scale = Math.min(Math.clamp(userScale, .65, 1.60),
            Math.min((guiWidth - 18.0) / WIDTH, (guiHeight - 16.0) / HEIGHT));
        double actualScale = scale * (1.0 + frame.pop());
        double x = Math.max(5, guiWidth - WIDTH * actualScale - Math.max(0, right));
        double y = Math.max(5, Math.min(top, guiHeight - HEIGHT * actualScale - 7));
        c.push(x, y + frame.lift(), actualScale);
        try { panel(c, state, frame); }
        finally { c.pop(); }
    }

    private static void panel(Canvas c, CrestHudState state, CrestHudFrame frame) {
        int accent = accent(state.kind());
        double opacity = frame.opacity();
        boolean alert = state.kind() == CrestHudState.Kind.MISSING || state.kind() == CrestHudState.Kind.SWITCH_CREST;

        // Same lilac floating shell used by the rarity reveal; no square scoreboard box.
        glow(c, -16, -11, WIDTH + 32, HEIGHT + 28, accent,
            (alert ? .19 + .07 * frame.attention() : .095) * opacity);
        round(c, -5, 2, WIDTH + 10, HEIGHT + 9, 23, 0xFF06050C, .12 * opacity);
        round(c, -2, 0, WIDTH + 4, HEIGHT + 4, 22, 0xFF06050C, .32 * opacity);
        round(c, 0, 0, WIDTH, HEIGHT, 20, alert ? accent : 0xFF494056, opacity);
        double border = alert ? 2 : 1;
        round(c, border, border, WIDTH - border * 2, HEIGHT - border * 2, 20 - border, PANEL, opacity);
        glow(c, 13, -6, 125, 69, 0xFFB69AFF, .11 * opacity);
        glow(c, 136, 49, 102, 67, accent, .075 * opacity);

        // Icon pill + hand-built paw, visually shared with Trevor's original rolling cards.
        round(c, 14, 12, 29, 29, 11, 0xFF363047, opacity);
        dot(c, 28.5, 27.5, 10, accent, .10 * opacity);
        paw(c, 28.5, 27, .59, accent, opacity);
        text(c, "Trevor's crest", 50, 15, 1.04, INK, opacity);
        text(c, "Every pelt counts.", 50, 30, .77, MUTED, opacity);

        String badge = state.badge();
        double badgeScale = .75;
        double badgeW = c.textWidth(badge) * badgeScale + 20;
        double bx = WIDTH - badgeW - 13;
        round(c, bx, 13, badgeW, 20, 10, accent, .105 * opacity);
        dot(c, bx + 10, 23, 2.1, accent, .92 * opacity);
        text(c, badge, bx + 17, 19.7, badgeScale, accent, opacity);
        round(c, 15, 48, WIDTH - 30, 1, 0, 0xFF61576C, .40 * opacity);

        switch (state.kind()) {
            case MISSING -> missing(c, accent, opacity);
            case COMMON -> progress(c, state, frame, "Let's get started!", accent, opacity, "COMMON  /  UNCOMMON");
            case ALMOST_THERE -> progress(c, state, frame, "Almost there!", accent, opacity, "UNCOMMON  /  RARE");
            case SWITCH_CREST -> completed(c, state, frame, accent, opacity);
        }
        if (frame.sparkle() > 0.02 && state.kind() != CrestHudState.Kind.MISSING) {
            sparkle(c, 204, 55, 2.3, accent, frame.sparkle() * opacity);
            sparkle(c, 213, 63, 1.35, accent, frame.sparkle() * .75 * opacity);
        }
    }

    private static void missing(Canvas c, int accent, double opacity) {
        text(c, "MOVE TRAPPER CREST", 17, 57, 1.10, accent, opacity);
        text(c, "TO YOUR HOTBAR!", 17, 74, 1.26, INK, opacity);
        dot(c, 189, 74, 17, accent, .065 * opacity);
        paw(c, 188, 74, .45, accent, .58 * opacity);
        round(c, 16, 97, 196, 1, 0, 0xFF504B64, .45 * opacity);
        text(c, "Use slots 1-9, first crest is active", 17, 104, .73, MUTED, opacity);
    }

    private static void progress(Canvas c, CrestHudState state, CrestHudFrame frame,
                                 String headline, int accent, double opacity, String stage) {
        text(c, headline, 17, 57, 1.11, accent, opacity);
        String count = state.peltsEarned() < 0 ? "--" : String.format(Locale.ROOT, "%,d", state.peltsEarned());
        String value = count + " / " + state.goal();
        double valueScale = Math.min(1.25, 125.0 / Math.max(1, c.textWidth(value)));
        text(c, value, 17, 73, valueScale, INK, opacity);
        String remaining = state.peltsEarned() < 0 ? "UNKNOWN" : state.remaining() + " TO GO";
        textRight(c, remaining, 211, 78, .73, accent, opacity);
        progressBar(c, frame.progress(), accent, opacity, frame.sparkle());
        text(c, stage, 18, 106, .73, MUTED, opacity);
        textRight(c, "ACTIVE " + (state.hotbarSlot() + 1), 210, 106, .73, accent, opacity);
    }

    private static void completed(Canvas c, CrestHudState state, CrestHudFrame frame, int accent, double opacity) {
        text(c, "500 PELTS REACHED", 17, 56, 1.03, accent, opacity);
        text(c, "SWITCH CRESTS!", 17, 72, 1.40, INK, opacity);
        progressBar(c, 1, accent, opacity, frame.sparkle());
        text(c, "Remove crest from slot " + (state.hotbarSlot() + 1), 18, 106, .82, accent, opacity);
    }

    private static void progressBar(Canvas c, double progress, int accent, double opacity, double sparkle) {
        final double bx = 17, by = 92, bw = 194, bh = 8;
        round(c, bx, by, bw, bh, 4, 0xFF303045, opacity);
        double fill = Math.clamp(progress, 0.0, 1.0) * bw;
        if (fill > 1) {
            // Two shades create a soft capsule with a slender glossy highlight.
            round(c, bx, by, fill, bh, Math.min(4, fill / 2), accent, .92 * opacity);
            if (fill > 6) round(c, bx + 3, by + 1, fill - 5, 1.5, .7, 0xFFFFFFFF, .18 * opacity);
            if (sparkle > .03 && fill < bw) dot(c, bx + fill, by + bh / 2, 4 + sparkle, accent, .30 * sparkle * opacity);
        }
    }

    private static int accent(CrestHudState.Kind kind) { return ACCENT[kind.ordinal()]; }
    private static void paw(Canvas c, double x, double y, double s, int color, double a) {
        ellipse(c, x - 7 * s, y - 1 * s, 14 * s, 11 * s, color, a);
        ellipse(c, x - 12 * s, y - 8 * s, 6 * s, 8 * s, color, a);
        ellipse(c, x - 5 * s, y - 13 * s, 6 * s, 8 * s, color, a);
        ellipse(c, x + 2 * s, y - 12 * s, 6 * s, 8 * s, color, a);
        ellipse(c, x + 8 * s, y - 6 * s, 6 * s, 8 * s, color, a);
    }
    private static void sparkle(Canvas c, double x, double y, double radius, int color, double alpha) {
        round(c, x - .6, y - radius, 1.2, radius * 2, .6, color, alpha);
        round(c, x - radius, y - .6, radius * 2, 1.2, .6, color, alpha);
    }
    private static void textRight(Canvas c, String text, double rightX, double y, double size, int color, double alpha) {
        text(c, text, rightX - c.textWidth(text) * size, y, size, color, alpha);
    }
    private static void dot(Canvas c, double x, double y, double r, int color, double alpha) {
        ellipse(c, x - r, y - r, r * 2, r * 2, color, alpha);
    }
    private static void round(Canvas c, double x, double y, double w, double h, double radius, int color, double alpha) {
        c.round(x, y, w, h, radius, tint(color, alpha));
    }
    private static void ellipse(Canvas c, double x, double y, double w, double h, int color, double alpha) {
        c.ellipse(x, y, w, h, tint(color, alpha));
    }
    private static void glow(Canvas c, double x, double y, double w, double h, int color, double alpha) {
        c.glow(x, y, w, h, tint(color, alpha));
    }
    private static void text(Canvas c, String text, double x, double y, double size, int color, double alpha) {
        c.text(text, x, y, size, tint(color, alpha));
    }
    private static int tint(int argb, double alpha) {
        return (argb & 0x00FFFFFF) | ((int) Math.round((argb >>> 24) * Math.clamp(alpha, 0, 1)) << 24);
    }
}
