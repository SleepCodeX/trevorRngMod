package dev.trevorroll;

/** Time-based menu motion: no tick timers, background work or per-frame allocations. */
public final class MenuMotion {
    public static final long OPEN_NANOS = 220_000_000L;
    private static final long REVOLUTION_NANOS = 24_000_000_000L;
    private MenuMotion() {}

    public static double openingOpacity(long elapsedNanos, boolean enabled) {
        if (!enabled || elapsedNanos >= OPEN_NANOS) return 1;
        double t = Math.clamp(elapsedNanos / (double) OPEN_NANOS, 0, 1);
        return 1 - (1 - t) * (1 - t) * (1 - t);
    }

    public static double iconAngle(long elapsedNanos, int module) {
        double phase = Math.floorMod(elapsedNanos, REVOLUTION_NANOS) / (double) REVOLUTION_NANOS;
        return phase * Math.PI * 2 + .38 + module * .11;
    }
}
