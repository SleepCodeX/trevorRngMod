package dev.trevorroll;

/** Time-based menu motion: no tick timers, background work or per-frame allocations. */
public final class MenuMotion {
    public static final long OPEN_NANOS = 260_000_000L;
    public static final long CLOSE_NANOS = 170_000_000L;
    private static final long REVOLUTION_NANOS = 24_000_000_000L;
    private MenuMotion() {}

    public static double openingOpacity(long elapsedNanos, boolean enabled) {
        if (!enabled || elapsedNanos >= OPEN_NANOS) return 1;
        double t = Math.clamp(elapsedNanos / (double) OPEN_NANOS, 0, 1);
        return 1 - (1 - t) * (1 - t) * (1 - t);
    }

    public static double closingOpacity(long elapsedNanos, boolean enabled) {
        if(!enabled)return 0;
        double t=Math.clamp(elapsedNanos/(double)CLOSE_NANOS,0,1);
        return 1-t*t*(3-2*t);
    }
    /** Exponential response has the same trajectory at 30, 60 and 144 Hz. */
    public static double approach(double value,double target,double seconds,double response) {
        return target+(value-target)*Math.exp(-Math.max(0,seconds)/response);
    }
    public static int mix(int a,int b,double t) {
        t=Math.clamp(t,0,1);int result=0;
        for(int shift=0;shift<=24;shift+=8)result|=(int)Math.round(((a>>>shift)&255)*(1-t)+((b>>>shift)&255)*t)<<shift;
        return result;
    }
    public static double iconAngle(long elapsedNanos, int module) {
        double phase = Math.floorMod(elapsedNanos, REVOLUTION_NANOS) / (double) REVOLUTION_NANOS;
        return phase * Math.PI * 2 + .38 + module * .11;
    }
}
