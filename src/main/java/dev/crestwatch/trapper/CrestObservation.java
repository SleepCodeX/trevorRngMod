package dev.crestwatch.trapper;

/** Data read directly from the Trapper Crest present in one hotbar slot. */
public record CrestObservation(CrestTier tier, int peltsEarned, int slot) {
    public CrestObservation {
        if (tier == null) throw new IllegalArgumentException("tier");
        if (peltsEarned < -1) throw new IllegalArgumentException("peltsEarned");
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("hotbar slot");
    }
}
