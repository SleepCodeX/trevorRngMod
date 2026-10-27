package dev.trevorroll;

/** Pure geometry: screen-space snapping and aspect-preserving corner resizing. */
public final class EditorGeometry {
    private EditorGeometry() {}
    public record Snap(double position, double guide) {}
    public static Snap snap(double proposed, double size, double screen, double otherStart,
                            double otherSize, int grid, double threshold) {
        double best = threshold, target = proposed, guide = Double.NaN;
        double[] stops = {12, screen / 2, screen - 12, otherStart, otherStart + otherSize / 2, otherStart + otherSize};
        for (double edge : new double[]{0, size / 2, size}) {
            for (double stop : stops) {
                double distance = Math.abs(proposed + edge - stop);
                if (distance < best) { best = distance; target = stop - edge; guide = stop; }
            }
        }
        if (Double.isNaN(guide) && grid > 0) target = Math.round(proposed / grid) * (double) grid;
        return new Snap(target, guide);
    }
    public static double resize(double dx, double dy, double logicalWidth, double logicalHeight, double min, double max) {
        // Projection on the aspect-ratio diagonal avoids jumps when either axis leads.
        double scale = (dx * logicalWidth + dy * logicalHeight)
            / (logicalWidth * logicalWidth + logicalHeight * logicalHeight);
        return HudLayout.finiteClamp(scale, min, max, min);
    }
}
