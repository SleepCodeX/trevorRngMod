package dev.trevorroll;

/** Deterministic opening/rotation checks independent of frame rate and game startup. */
public final class MenuMotionTest {
    public static void main(String[] args) {
        require(MenuMotion.openingOpacity(0, true) == 0, "Fade starts transparent");
        require(MenuMotion.openingOpacity(-1, true) == 0, "Negative elapsed time is clamped");
        require(MenuMotion.openingOpacity(MenuMotion.OPEN_NANOS, true) == 1, "Fade ends exactly");
        require(MenuMotion.openingOpacity(Long.MAX_VALUE, true) == 1, "Old screens remain visible");
        require(MenuMotion.openingOpacity(0, false) == 1, "Reduced motion bypasses the fade");
        double previous = 0;
        for (long elapsed = 0; elapsed <= MenuMotion.OPEN_NANOS; elapsed += 1_000_000) {
            double opacity = MenuMotion.openingOpacity(elapsed, true);
            require(opacity >= previous && opacity <= 1, "Fade is monotonic and bounded");
            previous = opacity;
        }
        for (int module = 0; module < 8; module++) {
            double a = MenuMotion.iconAngle(0, module);
            require(Math.abs(MenuMotion.iconAngle(24_000_000_000L, module) - a) < 1e-9, "Rotation loops without drift");
            require(Math.abs(MenuMotion.iconAngle(12_000_000_000L, module) - a - Math.PI) < 1e-9, "Slow half rotation");
            require(Double.isFinite(MenuMotion.iconAngle(Long.MAX_VALUE, module)), "Rotation remains finite");
        }
        System.out.println("PASS: menu fade timing, reduced motion, bounded easing and slow frame-independent icon rotation");
    }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
