package dev.trevorroll;

import java.util.*;

/** Time-based UI reward with a lit, perspective-projected golden paw medallion. */
public final class ElusiveRewardPainter {
    private ElusiveRewardPainter() {}
    private static final int GOLD = 0xFFFFC75B, WHITE = 0xFFFFF3C9;
    private record V(double x, double y, double z) {}
    private record Face(V[] vertices, int color) {}
    private record Projected(double[] x, double[] y, double depth, int color) {}
    private static final List<Face> MEDAL = medal();

    public static void draw(HudPainter.Canvas c, ElusiveReward reward, long now, int width, int height) {
        if (reward.expired(now) || now < reward.startedAt() || width < 160 || height < 100) return;
        double t = reward.progress(now), fade = smooth(t / .08) * (1 - smooth((t - .82) / .18));
        if (fade <= 0) return;
        double s = Math.min(width / 480., height / 270.);
        int level = reward.style().ordinal();
        if (level > 0) c.round(0, 0, width, height, 0, alpha(0xFF0C0B1E, fade * (.1 + level * .045) * reward.intensity()));
        c.push((width - 480 * s) / 2, (height - 270 * s) / 2, s);
        try {
            double seconds = (now - reward.startedAt()) / 1000.;
            if (level == 0) {
                double y = 27 - 9 * (1 - smooth(t / .15));
                c.glow(188, y - 12, 104, 65, alpha(GOLD, .18 * fade));
                c.round(174, y, 132, 34, 12, alpha(0xFF302236, fade));
                c.round(175, y + 1, 130, 32, 11, alpha(0xFF191625, fade));
                coin(c, 196, y + 17, .32, .18 * Math.sin(seconds * 3), 0, fade);
                center(c, "ELUSIVE!", 254, y + 12, 1.28, alpha(GOLD, fade));
                for (int i = 0; i < 4; i++) star(c, 166 + i * 49, y + 16 + Math.sin(seconds * 3 + i) * 9, 1.8, alpha(WHITE, fade * .7));
                return;
            }
            double entry = Math.clamp(t / .27, 0, 1);
            double settle = 1 - Math.pow(1 - entry, 3);
            double pop = 1 + Math.sin(entry * Math.PI) * (level >= 2 ? .22 : .08);
            double size = (.25 + .75 * settle) * pop * (level == 3 ? 1.42 : level == 2 ? 1.18 : .94);
            double cy = 104 + Math.sin(seconds * 2) * 2 + (1 - settle) * 35;
            c.glow(106, cy - 106, 268, 215, alpha(GOLD, fade * (.22 + .07 * level)));
            c.glow(160, cy - 65, 160, 140, alpha(0xFFFF9D40, fade * .2));
            if (level >= 2) {
                int rays = level == 3 ? 32 : 18;
                for (int i = 0; i < rays; i++) {
                    double a = i * Math.PI * 2 / rays + seconds * .05;
                    double inner = 57 + 8 * Math.sin(i * 2.1), outer = inner + (level == 3 ? 67 : 38);
                    ray(c, 240, cy, a, inner, outer, alpha(GOLD, fade * .19 * reward.intensity()));
                }
                double burst = smooth(t / .32);
                ring(c, 240, cy, 29 + burst * (level == 3 ? 112 : 86), .62,
                    alpha(WHITE, fade * (1 - burst) * .9), 0);
            }
            ring(c, 240, cy, 58 + level * 9, .33, alpha(GOLD, fade * .8), seconds * .35);
            if (level == 3) {
                ring(c, 240, cy, 89, .8, alpha(WHITE, fade * .4), -seconds * .2);
                ring(c, 240, cy, 99, .55, alpha(GOLD, fade * .45), seconds * .25);
            }
            particles(c, reward, seconds, t, level, fade, cy);
            double yaw = (1 - settle) * Math.PI * (level == 3 ? 6 : 4) + Math.sin(seconds * 1.8) * .2;
            coin(c, 240, cy, size, yaw, Math.sin(seconds * 1.5) * .07, fade);
            // A clean type lockup, with a warm extruded shadow rather than a square panel.
            double textScale = (level == 3 ? 2.8 : level == 2 ? 2.5 : 2.1) * (.85 + .15 * settle);
            double ty = level == 3 ? 172 : 165;
            center(c, "ELUSIVE!", 240, ty + 3, textScale, alpha(0xFF6E351E, fade));
            center(c, "ELUSIVE!", 240, ty, textScale, alpha(GOLD, fade));
            center(c, "A little luck. A rare hunt.", 240, ty + 29, .9, alpha(WHITE, fade * .9));
            String location = reward.location().isBlank() ? "THE HUNT IS ON" : "Go to " + reward.location();
            double fs = Math.min(.9, 300 / Math.max(1, c.textWidth(location)));
            center(c, location, 240, ty + 45, fs, alpha(0xFFD7CCEB, fade * .85));
        } finally { c.pop(); }
    }
    private static void particles(HudPainter.Canvas c, ElusiveReward r, double seconds, double t, int level, double fade, double cy) {
        int count = (int) ((level == 1 ? 14 : level == 2 ? 46 : 80) * r.intensity());
        for (int i = 0; i < count; i++) {
            double a = i * 2.399963 + seconds * (i % 2 == 0 ? .12 : -.08);
            double d = 42 + (i * 31 % 85) * smooth(t / .32);
            double x = 240 + Math.cos(a) * d * 1.28, y = cy + Math.sin(a) * d * .72 + t * t * 13;
            double blink = .45 + .55 * Math.pow(Math.sin(seconds * 2.6 + i * 1.7), 2);
            int color = alpha(i % 5 == 0 ? 0xFFE5C4FF : i % 3 == 0 ? WHITE : GOLD, fade * blink * .8);
            double size = (i % 6 == 0 ? 3.4 : 1.3) * (level == 3 ? 1.2 : 1);
            if (i % 3 == 0) star(c, x, y, size, color);
            else c.round(x, y, size * 1.4, size * .8, .3, color);
        }
    }
    private static void ring(HudPainter.Canvas c, double x, double y, double r, double squash, int color, double phase) {
        for (int i = 0; i < 100; i++) {
            double a = i * Math.PI * 2 / 100 + phase;
            double xx = x + Math.cos(a) * r, yy = y + Math.sin(a) * r * squash;
            c.ellipse(xx - .8, yy - .8, 1.6, 1.6, color);
        }
    }
    private static void ray(HudPainter.Canvas c, double x, double y, double angle, double inner, double outer, int color) {
        for (double d = inner; d < outer; d += 2) {
            c.round(x + Math.cos(angle) * d, y + Math.sin(angle) * d * .72, 1.5, 1.5, .3,
                alpha(color, 1 - (d - inner) / (outer - inner)));
        }
    }
    private static void star(HudPainter.Canvas c, double x, double y, double s, int color) {
        c.round(x - s, y - .7, 2 * s, 1.4, .6, color);
        c.round(x - .7, y - s, 1.4, 2 * s, .6, color);
    }
    private static void center(HudPainter.Canvas c, String text, double x, double y, double s, int color) {
        c.text(text, x - c.textWidth(text) * s / 2, y, s, color);
    }
    private static List<Face> medal() {
        List<Face> faces = new ArrayList<>();
        extrude(faces, 0, 0, 37, 37, 5, -5, 20, 0xFFD18A2E, 0xFFFFC657);
        extrude(faces, 0, 0, 32, 32, -5, -7, 20, 0xFFB06D28, 0xFFEA9C32);
        extrude(faces, 0, 8, 14, 11, -7, -12, 12, 0xFFC78A35, 0xFFFFE5A0);
        for (double[] toe : new double[][]{{-17,-4,5.8,7.5},{-7,-16,6,8},{7,-16,6,8},{17,-4,5.8,7.5}})
            extrude(faces, toe[0], toe[1], toe[2], toe[3], -7, -12, 10, 0xFFC78A35, 0xFFFFE5A0);
        return List.copyOf(faces);
    }
    private static void extrude(List<Face> faces, double x, double y, double rx, double ry, double back, double front, int n, int side, int top) {
        V[] f = new V[n], b = new V[n];
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            f[i] = new V(x + Math.cos(a) * rx, y + Math.sin(a) * ry, front);
            b[i] = new V(f[i].x, f[i].y, back);
        }
        faces.add(new Face(b, side)); faces.add(new Face(f, top));
        for (int i = 0; i < n; i++) faces.add(new Face(new V[]{b[i], b[(i+1)%n], f[(i+1)%n], f[i]}, shade(side, .72 + .28 * (1 - Math.sin(i * 2 * Math.PI / n)) / 2)));
    }
    private static void coin(HudPainter.Canvas c, double cx, double cy, double scale, double yaw, double roll, double opacity) {
        List<Projected> faces = new ArrayList<>(MEDAL.size());
        double cos = Math.cos(yaw), sin = Math.sin(yaw), cr = Math.cos(roll), sr = Math.sin(roll);
        for (Face f : MEDAL) {
            double[] xs = new double[f.vertices.length], ys = new double[xs.length]; double depth = 0;
            for (int i = 0; i < xs.length; i++) {
                V v = f.vertices[i];
                double x = v.x * cos + v.z * sin, z = -v.x * sin + v.z * cos;
                double y = v.y * .985 - z * .17; z = v.y * .17 + z * .985;
                double xx = x * cr - y * sr, yy = x * sr + y * cr, p = 300 / (300 + z);
                xs[i] = cx + xx * p * scale; ys[i] = cy + yy * p * scale; depth += z;
            }
            faces.add(new Projected(xs, ys, depth / xs.length, alpha(f.color, opacity)));
        }
        faces.sort(Comparator.comparingDouble(Projected::depth).reversed());
        for (Projected f : faces) polygon(c, f);
    }
    private static void polygon(HudPainter.Canvas c, Projected f) {
        double min = Arrays.stream(f.y).min().orElse(0), max = Arrays.stream(f.y).max().orElse(0);
        for (double y = Math.floor(min); y <= max; y++) {
            double left = Double.POSITIVE_INFINITY, right = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < f.x.length; i++) {
                int j = (i+1) % f.x.length;
                if ((f.y[i] <= y && f.y[j] > y) || (f.y[j] <= y && f.y[i] > y)) {
                    double x = f.x[i] + (y - f.y[i]) * (f.x[j] - f.x[i]) / (f.y[j] - f.y[i]);
                    left = Math.min(left, x); right = Math.max(right, x);
                }
            }
            if (right > left) c.round(left, y, right - left + .2, 1.05, 0, f.color);
        }
    }
    private static int shade(int color, double s) {
        return 0xFF000000 | (int)(((color >> 16) & 255) * s) << 16 | (int)(((color >> 8) & 255) * s) << 8 | (int)((color & 255) * s);
    }
    private static int alpha(int color, double a) { return (color & 0xFFFFFF) | ((int)Math.round((color >>> 24) * Math.clamp(a, 0, 1)) << 24); }
    private static double smooth(double t) { t = Math.clamp(t, 0, 1); return t * t * (3 - 2 * t); }
}
