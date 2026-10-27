package dev.trevorroll;

import java.util.Random;

/** Cosmetic cards have no influence on the server-assigned winning card. */
public final class Reel {
    public static final int TARGET = 31;
    public static final int CARD_PITCH = 90;
    public static final int CARD_WIDTH = 80;
    public static final int RESULT_MILLIS = 4500;
    public final Hunt hunt;
    public final Hunt.Rarity[] cards = new Hunt.Rarity[TARGET + 8];
    public final long startedAt;
    public final int durationMillis;
    public final boolean demo;
    public final int resultMillis;
    public boolean announced;
    public int lastClick = -1;

    public Reel(Hunt hunt, long startedAt, int durationMillis, boolean demo, Random random) {
        this(hunt, startedAt, durationMillis, demo, random, RESULT_MILLIS);
    }

    public Reel(Hunt hunt, long startedAt, int durationMillis, boolean demo, Random random, int resultMillis) {
        this.resultMillis = Math.clamp(resultMillis, 1000, 10000);
        this.hunt = hunt;
        this.startedAt = startedAt;
        this.durationMillis = durationMillis;
        this.demo = demo;
        var rarities = Hunt.Rarity.values();
        for (int i = 0; i < cards.length; i++) cards[i] = rarities[random.nextInt(rarities.length)];
        cards[TARGET] = hunt.rarity();
    }

    public double progress(long now) {
        return Math.clamp((now - startedAt) / (double) durationMillis, 0.0, 1.0);
    }

    public double position(long now) {
        double t = progress(now);
        // Cubic ease-out: moves quickly, then settles exactly at the true result.
        return 3.0 + (TARGET - 3.0) * (1.0 - Math.pow(1.0 - t, 3.0));
    }

    public boolean finished(long now) { return now - startedAt >= durationMillis; }
    public boolean expired(long now) { return now - startedAt >= durationMillis + resultMillis; }
}
