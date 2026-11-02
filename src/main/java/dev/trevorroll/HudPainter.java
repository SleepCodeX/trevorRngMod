package dev.trevorroll;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared drawing code for the Minecraft HUD and the offline visual preview. */
public final class HudPainter {
    public static final double WIDTH = 414, HEIGHT = 160;
    private static final double PITCH = Reel.CARD_PITCH, CARD = Reel.CARD_WIDTH;
    private static final int INK = 0xFFF6F2FF, MUTED = 0xFFA5A1C0, PANEL = 0xFF18182C;
    private static final int[] COLORS = {0xFFDCE5F4, 0xFF91E4BD, 0xFF8FC5FF, 0xFFC7A2FF, 0xFFFFD28D};

    public interface Canvas {
        void push(double x, double y, double scale);
        void pop();
        void round(double x, double y, double w, double h, double radius, int color);
        void ellipse(double x, double y, double w, double h, int color);
        void glow(double x, double y, double w, double h, int color);
        void text(String text, double x, double y, double scale, int color);
        double textWidth(String text);
        void clip(double x, double y, double w, double h);
        void unclip();
        default boolean peltCoin(double x,double y,double radius,Hunt.Rarity rarity,double opacity){return false;}
        default void horizontalFade(double x,double y,double w,double h,int color,boolean reverse){
            for(int i=0;i<22;i++){double amount=reverse?i/22.:1-i/22.;round(x+i*w/22.,y,w/22.+.1,h,0,(color&0xFFFFFF)|((int)((color>>>24)*amount)<<24));}
        }
        default boolean medallion(double x,double y,double scale,double yaw,double roll,double opacity) { return false; }
    }

    private final Canvas c;
    private final double opacity;
    private HudPainter(Canvas canvas, double opacity) { this.c = canvas; this.opacity = opacity; }

    public static void draw(Canvas c, Reel reel, long now, int screenWidth, int screenHeight) {
        draw(c, reel, now, screenWidth, screenHeight, 1, .5, 0, false);
    }

    public static void draw(Canvas c, Reel reel, long now, int screenWidth, int screenHeight,
                            double userScale, double x, double y, boolean customPosition) {
        if (reel.expired(now) || screenWidth < 100 || screenHeight < 80) return;
        double age = Math.max(0, now - reel.startedAt);
        double remaining = reel.durationMillis + reel.resultMillis - age;
        double alpha = smooth(age / 180) * smooth(remaining / 350);
        var bounds = HudLayout.roll(screenWidth, screenHeight, userScale, x, y, customPosition);
        double scale = bounds.scale();
        c.push(bounds.x(), bounds.y() - (1 - smooth(age / 240)) * 8, scale);
        try { new HudPainter(c, alpha).panel(reel, now); }
        finally { c.pop(); }
    }

    private void panel(Reel reel, long now) {
        boolean revealed = reel.finished(now);
        double since = Math.max(0, now - reel.startedAt - reel.durationMillis) / 1000.0;
        double reveal = revealed ? smooth(since / .3) : 0;
        int accent = revealed ? color(reel.hunt.rarity()) : 0xFFBCA8FF;

        // Layered, soft shadows; one restrained lavender edge around the shell.
        round(-7, 0, WIDTH + 14, HEIGHT + 14, 26, 0x16040510);
        round(-3, 1, WIDTH + 6, HEIGHT + 5, 23, 0x38040510);
        round(0, 0, WIDTH, HEIGHT, 21, 0xFF393347);
        round(1, 1, WIDTH - 2, HEIGHT - 2, 20, PANEL);
        glow(30, -1, 155, 78, tint(0xFFB69AFF, .10));
        glow(268, 73, 124, 78, tint(accent, .065));

        // Small badge, calm heading, and a single status chip.
        round(16, 14, 25, 25, 9, 0xFF343047);
        paw(28.5, 26, .54, 0xFFD8C7FF);
        text("Trevor's hunt", 49, 16, 1, INK);
        text(revealed?PeltCoins.tagline(reel.hunt.rarity()):"A little luck. A new hunt.", 49, 29, .65, revealed?accent:MUTED);
        String status = reel.demo ? "Preview" : revealed ? "Revealed" : "Rolling";
        round(314, 16, 82, 21, 10.5, tint(accent, .11));
        dot(325, 26.5, 2, accent);
        text(status, 333, 23, .77, accent);
        if (!revealed && !reel.demo) {
            double pulse = .45 + .45 * Math.sin((now - reel.startedAt) / 160.0);
            dot(383, 26, 1.3, tint(accent, pulse));
        }

        c.clip(12, 46, WIDTH - 24, 74);
        try {
            double position = reel.position(now);
            for (int i = 0; i < reel.cards.length; i++) {
                double offset = (i - position) * PITCH;
                double x = WIDTH / 2 + offset - CARD / 2;
                if (x > WIDTH - 8 || x + CARD < 8) continue;
                double focus = Math.max(0, 1 - Math.abs(offset) / (PITCH * 1.6));
                boolean winner = revealed && i == Reel.TARGET;
                double visibility = (.47 + .53 * focus) * (winner ? 1 : 1 - reveal * .5);
                double bounce = winner ? 3.2 * Math.exp(-since * 6) * Math.sin(since * 15) : 0;
                card(x, 50 - focus * 2.5 - bounce, reel.cards[i], visibility, focus, winner, since);
            }
            // Feather the cut-off cards into the shell instead of a hard crop.
            c.horizontalFade(12,46,22,74,tint(PANEL,opacity*.92),false);
            c.horizontalFade(WIDTH-34,46,22,74,tint(PANEL,opacity*.92),true);
        } finally { c.unclip(); }

        // The pointer sits outside the cards so it never crosses the text/icon.
        glow(WIDTH / 2 - 11, 37, 22, 18, tint(accent, .35));
        round(WIDTH / 2 - 4, 43, 8, 4, 2, accent);
        dot(WIDTH / 2, 117, 1.5, tint(accent, .8));

        if (revealed) {
            double footer = smooth(since / .25);
            String rarityName=PeltCoins.name(reel.hunt.rarity());
            center("Go hunt "+rarityName,WIDTH/2,126+(1-footer)*4,.88,tint(accent,footer));
            List<String> lines = wrap(reel.hunt.location(), 365, .75);
            double y = 140 + (1 - footer) * 4;
            for (String line : lines) {
                center(line, WIDTH / 2, y, .75, tint(INK, footer));
                y += 11;
            }

            if (since < 1.1) sparkles(since, accent);
        } else {
            center("A little suspense...", WIDTH / 2, 132, .79, MUTED);
            for (int i = 0; i < 3; i++) {
                double lift = 1.3 * Math.sin((now - reel.startedAt) / 170.0 - i * .8);
                dot(WIDTH / 2 - 6 + i * 6, 147 + lift, 1.4, tint(0xFFCDBDFF, .65));
            }
        }
    }

    private void card(double x, double y, Hunt.Rarity rarity, double visibility, double focus, boolean winner, double since) {
        int accent = color(rarity);
        if (winner) glow(x - 12, y - 9, CARD + 24, 86, tint(accent, .22 + .15 * Math.exp(-since * 4)));
        round(x,y+3,CARD,64,9,tint(0xFF080B13,visibility*.7));
        round(x,y,CARD,64,9,tint(mix(0xFF465064,accent,winner?.9:.58),visibility));
        round(x+1,y+1,CARD-2,62,8,tint(mix(0xFF151D2C,accent,winner?.2:.09),visibility));
        round(x+4,y+2,CARD-8,1,0,tint(mix(accent,INK,.45),visibility*.55));
        center(PeltCoins.tier(rarity),x+40,y+6,.47,tint(accent,visibility*.85));
        if(!c.peltCoin(x+40,y+27,13.5,rarity,opacity*visibility))PeltCoins.draw(c,x+40,y+27,13.5,rarity,opacity*visibility);
        int band=tint(mix(0xFF1B2233,accent,.35),visibility);
        round(x+1,y+45,CARD-2,18,8,band);round(x+1,y+45,CARD-2,8,0,band);
        center(PeltCoins.name(rarity),x+40,y+50,.79,tint(mix(accent,INK,.3),visibility));
        if (winner) {
            star(x + 64, y + 12, 2.7, tint(accent, .9));
            star(x + 15, y + 34, 1.6, tint(accent, .65));
        }
    }

    private void paw(double x, double y, double s, int color) {
        ellipse(x - 7 * s, y - 1 * s, 14 * s, 11 * s, color);
        ellipse(x - 12 * s, y - 8 * s, 6 * s, 8 * s, color);
        ellipse(x - 5 * s, y - 13 * s, 6 * s, 8 * s, color);
        ellipse(x + 2 * s, y - 12 * s, 6 * s, 8 * s, color);
        ellipse(x + 8 * s, y - 6 * s, 6 * s, 8 * s, color);
    }

    private void sparkles(double seconds, int accent) {
        double t = Math.clamp(seconds / 1.1, 0, 1);
        double radius = 21 + 38 * (1 - Math.pow(1 - t, 3));
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI * 2 / 10 + .2;
            double x = WIDTH / 2 + Math.cos(angle) * radius * 1.38;
            double y = 80 + Math.sin(angle) * radius * .67 + t * t * 9;
            int color = tint(i % 3 == 0 ? INK : accent, Math.pow(1 - t, 1.5) * .85);
            if (i % 2 == 0) star(x, y, 2.4 * (1 - t * .45), color);
            else dot(x, y, 1.7 * (1 - t * .4), color);
        }
    }

    private void star(double x, double y, double r, int color) {
        round(x - .7, y - r, 1.4, r * 2, .7, color);
        round(x - r, y - .7, r * 2, 1.4, .7, color);
    }
    private void dot(double x, double y, double radius, int color) { ellipse(x - radius, y - radius, radius * 2, radius * 2, color); }
    private void round(double x, double y, double w, double h, double r, int color) { c.round(x, y, w, h, r, tint(color, opacity)); }
    private void ellipse(double x, double y, double w, double h, int color) { c.ellipse(x, y, w, h, tint(color, opacity)); }
    private void glow(double x, double y, double w, double h, int color) { c.glow(x, y, w, h, tint(color, opacity)); }
    private void text(String value, double x, double y, double size, int color) { c.text(value, x, y, size, tint(color, opacity)); }
    private void center(String value, double x, double y, double size, int color) { text(value, x - c.textWidth(value) * size / 2, y, size, color); }

    private List<String> wrap(String text, double width, double size) {
        List<String> lines = new ArrayList<>();
        String current = "";
        for (String word : text.split(" ")) {
            String next = current.isEmpty() ? word : current + " " + word;
            if (!current.isEmpty() && c.textWidth(next) * size > width) { lines.add(current); current = word; }
            else current = next;
        }
        if (!current.isEmpty()) lines.add(current);
        return lines;
    }

    private static int color(Hunt.Rarity rarity) { return PeltCoins.color(rarity); }
    private static double smooth(double t) { t = Math.clamp(t, 0, 1); return t * t * (3 - 2 * t); }
    private static int tint(int color, double alpha) { return (color & 0xFFFFFF) | ((int) Math.round((color >>> 24) * Math.clamp(alpha, 0, 1)) << 24); }
    private static int mix(int a, int b, double t) {
        int out = 0xFF000000;
        for (int shift : new int[] {16, 8, 0}) out |= (int) Math.round(((a >> shift) & 255) * (1 - t) + ((b >> shift) & 255) * t) << shift;
        return out;
    }
}
