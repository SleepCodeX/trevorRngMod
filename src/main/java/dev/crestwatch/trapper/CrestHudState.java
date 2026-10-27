package dev.crestwatch.trapper;

import java.util.List;

/**
 * The leftmost crest is active, regardless of the stage or count of later crests.
 * Recomputed when hotbar item components change; no sticky completion warnings.
 */
public record CrestHudState(Kind kind, int peltsEarned, int goal, int hotbarSlot) {
    public enum Kind { MISSING, COMMON, ALMOST_THERE, SWITCH_CREST }

    public static CrestHudState fromHotbar(List<CrestObservation> observations) {
        CrestObservation best = null;
        if (observations != null) for (var observation : observations) {
            if (observation != null && observation.slot() >= 0 && observation.slot() < 9
                    && (best == null || observation.slot() < best.slot())) best = observation;
        }
        if (best == null) {
            return new CrestHudState(Kind.MISSING, -1, 100, -1);
        }
        if (best.tier() == CrestTier.RARE || best.peltsEarned() >= 500) {
            return new CrestHudState(Kind.SWITCH_CREST, Math.max(500, best.peltsEarned()), 500, best.slot());
        }
        return switch (best.tier()) {
            case RARE -> new CrestHudState(Kind.SWITCH_CREST, Math.max(500, best.peltsEarned()), 500, best.slot());
            case UNCOMMON -> new CrestHudState(Kind.ALMOST_THERE, best.peltsEarned(), 500, best.slot());
            case COMMON -> new CrestHudState(Kind.COMMON, best.peltsEarned(), 100, best.slot());
        };
    }

    public String badge() {
        return switch (kind) {
            case MISSING -> "NO CREST";
            case COMMON -> "COMMON";
            case ALMOST_THERE -> "UNCOMMON";
            case SWITCH_CREST -> "SWITCH NOW";
        };
    }

    public String message() {
        return switch (kind) {
            case MISSING -> "Move a Trapper Crest to your hotbar!";
            case COMMON -> "Common Trapper Crest";
            case ALMOST_THERE -> "Almost there!";
            case SWITCH_CREST -> "500 Pelts Gained! Switch Crests!";
        };
    }

    public int remaining() {
        return Math.max(0, goal - Math.max(0, peltsEarned));
    }

    public double ratio() {
        return peltsEarned < 0 ? 0.0 : Math.min(1.0, Math.max(0.0, (double) peltsEarned / goal));
    }
}
