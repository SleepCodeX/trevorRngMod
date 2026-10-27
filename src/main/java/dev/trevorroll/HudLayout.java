package dev.trevorroll;

/** Shared screen-space bounds for rendering, resizing and editor hit testing. */
public final class HudLayout {
    private HudLayout() {}
    public record Bounds(double x, double y, double width, double height, double scale) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px <= x + width && py <= y + height;
        }
    }
    public static double finiteClamp(double value, double low, double high, double fallback) {
        return Double.isFinite(value) ? Math.max(low, Math.min(high, value)) : fallback;
    }
    public static Bounds roll(int width, int height, double requestedScale, double x, double y, boolean custom) {
        double scale = Math.max(.01, Math.min(finiteClamp(requestedScale, .5, 2, 1),
            Math.min((width - 24.0) / 414, (height - 24.0) / 160)));
        double w = 414 * scale, h = 160 * scale;
        double left = custom ? 12 + finiteClamp(x, 0, 1, .5) * Math.max(0, width - w - 24) : (width - w) / 2;
        double top = custom ? 12 + finiteClamp(y, 0, 1, 0) * Math.max(0, height - h - 24)
            : Math.min(Math.max(10, Math.min(28, height * .075)), height - h - 10);
        return new Bounds(left, top, w, h, scale);
    }
    public static Bounds crest(int width, int height, double requestedScale, int top, int right) {
        double scale = Math.max(.01, Math.min(finiteClamp(requestedScale, .65, 1.6, 1),
            Math.min((width - 18.0) / 228, (height - 16.0) / 118)));
        double w = 228 * scale, h = 118 * scale;
        return new Bounds(Math.max(5, width - w - Math.max(0, right)),
            Math.max(5, Math.min(top, height - h - 7)), w, h, scale);
    }
}
