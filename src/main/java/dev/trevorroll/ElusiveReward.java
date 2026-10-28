package dev.trevorroll;

/** Cosmetic reward state. The winning rarity remains the server's assignment. */
public record ElusiveReward(long startedAt, int durationMillis, Style style, double intensity, double volume, String location) {
    public enum Style {
        MINIMAL("Minimal"), CLASSIC("Classic"), EXTRA("Extra Reward"), JACKPOT("Jackpot");
        public final String label;
        Style(String label) { this.label = label; }
    }
    public ElusiveReward {
        durationMillis = Math.clamp(durationMillis, 1500, 10000);
        style = style == null ? Style.EXTRA : style;
        intensity = HudLayout.finiteClamp(intensity, .25, 1, .8);
        volume = HudLayout.finiteClamp(volume, 0, 1, .7);
        location = location == null ? "" : location;
    }
    public static boolean eligible(Hunt.Rarity rarity, boolean enabled) {
        return enabled && rarity == Hunt.Rarity.ELUSIVE;
    }
    public boolean expired(long now) { return now - startedAt >= durationMillis; }
    public double progress(long now) { return Math.clamp((now - startedAt) / (double) durationMillis, 0, 1); }
}
