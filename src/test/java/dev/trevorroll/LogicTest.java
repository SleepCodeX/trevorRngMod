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
        System.out.println("Trevor Roll logic checks passed: " + checks);
    }
}
