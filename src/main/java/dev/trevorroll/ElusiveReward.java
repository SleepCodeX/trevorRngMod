package dev.trevorroll;

/** Cosmetic reward state. The winning rarity remains the server's assignment. */
public record ElusiveReward(long startedAt, int durationMillis, Style style, double intensity, double volume, String location,
                           double sceneSecondsOffset) {
    public enum Style {
        MINIMAL("Minimal"), CLASSIC("Classic"), EXTRA("Extra"), JACKPOT("Extreme");
        public final String label;
        Style(String label) { this.label = label; }
    }
    public ElusiveReward(long startedAt,int durationMillis,Style style,double intensity,double volume,String location){
        this(startedAt,durationMillis,style,intensity,volume,location,0);
    }
    public ElusiveReward {
        durationMillis = Math.clamp(durationMillis, 1500, 10000);
        style = style == null ? Style.EXTRA : style;
        intensity = HudLayout.finiteClamp(intensity, .25, 1, .8);
        volume = HudLayout.finiteClamp(volume, 0, 1, .7);
        location = location == null ? "" : location;
        sceneSecondsOffset=HudLayout.finiteClamp(sceneSecondsOffset,0,60,0);
    }
    public static boolean eligible(Hunt.Rarity rarity, boolean enabled) {
        return enabled && rarity == Hunt.Rarity.ELUSIVE;
    }
    public boolean expired(long now) { return now - startedAt >= durationMillis; }
    public double progress(long now) { return Math.clamp((now - startedAt) / (double) durationMillis, 0, 1); }
}
