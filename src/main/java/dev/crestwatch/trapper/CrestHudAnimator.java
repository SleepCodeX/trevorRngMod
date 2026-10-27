package dev.crestwatch.trapper;

/**
 * Smooth progress changes and a short, restrained spring when the crest stage changes.
 * The *message* is always computed from the CURRENT hotbar state, never the previous state;
 * removing a Rare Crest therefore removes the 'switch crests' message immediately.
 * All time values are monotonic milliseconds.
 */
public final class CrestHudAnimator {
    private CrestHudState.Kind previousKind;
    private long transitionAtMs = Long.MIN_VALUE;
    private long lastFrameMs = -1;
    private double displayedRatio;
    private int previousPelts = -1;
    private int previousSlot = -1;
    private long peltSparkleAt = Long.MIN_VALUE;

    public CrestHudFrame sample(CrestHudState state, long nowMs) {
        if (state.kind() != previousKind || state.hotbarSlot() != previousSlot) {
            previousKind = state.kind();
            previousSlot = state.hotbarSlot();
            transitionAtMs = nowMs;
            displayedRatio = state.ratio(); // No 'uncommon to rare' bar rewind.
            previousPelts = state.peltsEarned();
            peltSparkleAt = nowMs;
        }
        double dt = lastFrameMs < 0 ? 0 : Math.clamp(nowMs - lastFrameMs, 0, 1000);
        lastFrameMs = nowMs;
        double amount = 1 - Math.exp(-dt / 180.0);
        displayedRatio += (state.ratio() - displayedRatio) * amount;
        if (Math.abs(displayedRatio - state.ratio()) < 0.0004) displayedRatio = state.ratio();
        if (state.peltsEarned() > previousPelts && previousPelts >= 0) {
            peltSparkleAt = nowMs;
        }
        previousPelts = state.peltsEarned();

        double elapsed = Math.max(0, nowMs - transitionAtMs);
        double t = Math.clamp(elapsed / 360.0, 0.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);
        double lift = -7 * (1 - ease); // subtle entrance from above
        double pop = 0.031 * Math.exp(-elapsed / 200.0) * Math.sin(elapsed * 0.023);
        double opacity = 0.78 + 0.22 * Math.clamp(elapsed / 170.0, 0, 1);
        double sparkleAge = Math.max(0, nowMs - peltSparkleAt);
        double sparkle = Math.max(0, 1 - sparkleAge / 720.0);
        boolean alert = state.kind() == CrestHudState.Kind.SWITCH_CREST || state.kind() == CrestHudState.Kind.MISSING;
        double attention = alert ? .5 + .5 * Math.sin(nowMs * (Math.PI * 2 / 1800.0)) : 0;
        return new CrestHudFrame(displayedRatio, lift, Math.max(0, pop), opacity, sparkle, attention);
    }
}
