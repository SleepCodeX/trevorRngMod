package dev.crestwatch.trapper;

/** Immutable, time-based HUD animation snapshot; no Minecraft dependencies. */
public record CrestHudFrame(double progress, double lift, double pop, double opacity, double sparkle, double attention) {
    public CrestHudFrame {
        progress = Math.clamp(progress, 0.0, 1.0);
        lift = Math.clamp(lift, -12.0, 12.0);
        pop = Math.clamp(pop, 0.0, 0.08);
        opacity = Math.clamp(opacity, 0.0, 1.0);
        sparkle = Math.clamp(sparkle, 0.0, 1.0);
        attention = Math.clamp(attention, 0.0, 1.0);
    }
}
