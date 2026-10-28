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
        var snap = EditorGeometry.snap(198, 100, 500, 70, 20, 0, 5);
        check(snap.position() == 200 && snap.guide() == 250, "panel center snaps to screen center");
        var grid = EditorGeometry.snap(131, 20, 800, 700, 10, 8, 2);
        check(grid.position() == 128 && Double.isNaN(grid.guide()), "grid snap without false guide");
        check(Math.abs(EditorGeometry.resize(228, 118, 228, 118, .65, 1.6) - 1) < .0001, "corner resize preserves aspect");
        check(EditorGeometry.resize(-100, -100, 228, 118, .65, 1.6) == .65, "corner cannot invert panel");
        check(EditorGeometry.resize(2000, 2000, 228, 118, .65, 1.6) == 1.6, "corner respects maximum size");
        var hold = new Reel(new Hunt(Hunt.Rarity.ELUSIVE, "Oasis"), 0, 1000, true, new Random(1), 6000);
        check(!hold.expired(6999) && hold.expired(7000), "custom result hold boundary");
        PeltTracker tracker=new PeltTracker();
        tracker.observe(new dev.crestwatch.trapper.CrestHudState(dev.crestwatch.trapper.CrestHudState.Kind.COMMON,0,100,0));
        tracker.advance(0,true);tracker.advance(1000,true);
        check(!tracker.observe(new dev.crestwatch.trapper.CrestHudState(dev.crestwatch.trapper.CrestHudState.Kind.ALMOST_THERE,250,500,0)),"mid crest does not complete");
        tracker.advance(2000,true);
        check(tracker.observe(new dev.crestwatch.trapper.CrestHudState(dev.crestwatch.trapper.CrestHudState.Kind.SWITCH_CREST,500,500,0)),"first 500 alerts once");
        check(!tracker.observe(new dev.crestwatch.trapper.CrestHudState(dev.crestwatch.trapper.CrestHudState.Kind.SWITCH_CREST,500,500,0)),"maxed crest does not repeat alert");
        check(tracker.earned()==500,"observed pelt deltas");
        tracker.advance(3000,false);tracker.advance(100000,false);check(tracker.elapsed()==2000,"offline time excluded");
        tracker.observe(new dev.crestwatch.trapper.CrestHudState(dev.crestwatch.trapper.CrestHudState.Kind.COMMON,0,100,1));
        check(tracker.earned()==500,"replacement does not add old pelts");
        tracker.start();check(tracker.earned()==0&&tracker.elapsed()==0,"manual reset clears rate session");
        System.out.println("Trevor Roll logic checks passed: " + checks);
    }
}
