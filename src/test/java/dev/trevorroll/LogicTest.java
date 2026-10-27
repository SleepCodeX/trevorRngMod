package dev.trevorroll;

import java.util.Random;

public final class LogicTest {
    private static int checks;
    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        var screenshot = Hunt.parse("§e[NPC] Trevor: §fYou can find your §9UNDETECTED §fanimal near the §aOvergrown Mushroom Cave.").orElseThrow();
        check(screenshot.rarity() == Hunt.Rarity.UNDETECTED, "screenshot rarity");
        check(screenshot.location().equals("Overgrown Mushroom Cave"), "screenshot location, no trailing period");
        check(Hunt.parse("[NPC] Trevor: You couldn't locate any animals.").isEmpty(), "failure dialogue not hidden");
        check(Hunt.parse("[MVP+] Someone: [NPC] Trevor: You can find your ELUSIVE animal near the Oasis.").isEmpty(), "player chat not hidden");
        check(Hunt.parse("[NPC] Trevor: You can find your UNKNOWN animal near the Oasis.").isEmpty(), "unknown format stays visible");
        check(Hunt.parse("[NPC] Trevor: You can find your ELUSIVE animal near the .").isEmpty(), "empty location stays visible");
        check(Hunt.parse("[NPC] Trevor: ✆ You can find your elusive animal in the Shepherd's Keep!").orElseThrow().location().equals("Shepherd's Keep"), "phone and alternate location wording");
        for (Hunt.Rarity rarity : Hunt.Rarity.values()) {
            Hunt hunt = Hunt.parse("[NPC] Trevor: You can find your " + rarity + " animal near the Oasis.").orElseThrow();
            check(hunt.rarity() == rarity, "parse " + rarity);
            for (int seed = 0; seed < 100; seed++) {
                Reel reel = new Reel(hunt, 5000L, 3000, false, new Random(seed));
                check(reel.cards[Reel.TARGET] == rarity, "true server result retained");
                double previous = reel.position(5000);
                for (long now = 5000; now <= 8000; now += 17) {
                    double position = reel.position(now);
                    check(position >= previous && position <= Reel.TARGET, "reel moves forward and never overshoots");
                    previous = position;
                }
                check(reel.position(8000) == Reel.TARGET, "lands on winner center");
                check(!reel.finished(7999) && reel.finished(8000), "reveal boundary");
                check(!reel.expired(12499) && reel.expired(12500), "result hold boundary");
                check(reel.position(0) == 3 && reel.position(20000) == Reel.TARGET, "time clamp");
            }
        }
        for (int width : new int[]{320, 854, 1920}) {
            for (int height : new int[]{240, 480, 1080}) {
                for (double scale : new double[]{.5, 1, 2, Double.NaN}) {
                    for (double x : new double[]{0, .5, 1}) {
                        var b = HudLayout.roll(width, height, scale, x, x, true);
                        check(b.x() >= 0 && b.y() >= 0 && b.x() + b.width() <= width
                            && b.y() + b.height() <= height, "custom roll fits after resize");
                        check(b.contains(b.x() + b.width() / 2, b.y() + b.height() / 2), "editor hitbox matches panel");
                        check(!b.contains(b.x() - 1, b.y()), "outside panel does not drag");
                    }
                    var c = HudLayout.crest(width, height, scale, 2000, 1000);
                    check(c.x() >= 0 && c.y() >= 0 && c.x() + c.width() <= width
                        && c.y() + c.height() <= height, "crest fits after resize");
                }
            }
        }
        check(HudLayout.finiteClamp(Double.POSITIVE_INFINITY, 0, 1, .5) == .5, "invalid saved values use default");
        System.out.println("Trevor Roll logic checks passed: " + checks);
    }
}
