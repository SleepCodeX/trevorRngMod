package dev.crestwatch.trapper;

import java.util.ArrayList;
import java.util.List;

/** Offline tests for floating HUD transitions, stage change, and text layout. */
public final class CrestHudAnimationTest {
    private static int checks;
    private static void ok(boolean works, String detail) {
        checks++;
        if (!works) throw new AssertionError(detail);
    }
    private static final class FakeCanvas implements CrestHudPainter.Canvas {
        final List<String> labels = new ArrayList<>();
        int roundCount;
        int glowCount;
        int ellipseCount;
        double px, py, ps = 1;
        @Override public void push(double x, double y, double scale) { }
        @Override public void pop() { }
        @Override public void round(double x, double y, double w, double h, double r, int c) {
            roundCount++; ok(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(w) && Double.isFinite(h), "finite panel geometry");
        }
        @Override public void ellipse(double x, double y, double w, double h, int c) { ellipseCount++; }
        @Override public void glow(double x, double y, double w, double h, int c) { glowCount++; }
        @Override public void text(String value, double x, double y, double size, int color) {
            labels.add(value);
            // Allow a 2px margin of error for pixel font metrics.
            ok(x >= 12 && x + textWidth(value) * size <= CrestHudPainter.WIDTH - 7,
                "Clipped text: " + value + " at " + x);
            ok(y >= 12 && y <= CrestHudPainter.HEIGHT - 8, "Text went off vertical panel: " + value);
            ok(size >= .7 && size <= 1.45, "Uncomfortable font scale " + size);
        }
        @Override public double textWidth(String t) { return t.length() * 5.35; }
        boolean contains(String needle) { return labels.stream().anyMatch(s -> s.contains(needle)); }
    }

    private static CrestHudState state(CrestHudState.Kind kind, int pelts, int goal) {
        return new CrestHudState(kind, pelts, goal, kind == CrestHudState.Kind.MISSING ? -1 : 2);
    }

    public static void main(String[] args) {
        long now = 1_000_000;
        CrestHudAnimator animator = new CrestHudAnimator();
        var common = state(CrestHudState.Kind.COMMON, 42, 100);
        CrestHudFrame first = animator.sample(common, now);
        ok(first.progress() == .42, "First frame must show actual lore progress");
        ok(first.opacity() > 0.7, "HUD should not be invisible while changing stages");
        CrestHudFrame later = animator.sample(common, now + 200);
        ok(later.lift() > first.lift(), "Card entrance motion settles correctly");
        var increased = state(CrestHudState.Kind.COMMON, 78, 100);
        CrestHudFrame growing = animator.sample(increased, now + 250);
        ok(growing.progress() > .42 && growing.progress() < .78, "Progress is animated rather than snapping");
        var settled = animator.sample(increased, now + 3000);
        ok(Math.abs(settled.progress() - .78) < .002, "Progress converges to actual lore count");
        var switchedSlot = new CrestHudState(CrestHudState.Kind.COMMON, 10, 100, 5);
        var switchFrame = animator.sample(switchedSlot, now + 3010);
        ok(Math.abs(switchFrame.progress() - .10) < .001, "Switching crests of the same tier resets the bar");
        var uncommon = state(CrestHudState.Kind.ALMOST_THERE, 258, 500);
        var afterTierChange = animator.sample(uncommon, now + 3100);
        ok(Math.abs(afterTierChange.progress() - .516) < .001, "Tier changes should show exact actual progress");
        var rare = state(CrestHudState.Kind.SWITCH_CREST, 504, 500);
        animator.sample(rare, now + 3200);
        var removedRare = state(CrestHudState.Kind.MISSING, -1, 100);
        var immediately = animator.sample(removedRare, now + 3201);
        ok(immediately.progress() == 0, "Progress is immediately cleared after removing Rare");
        ok(removedRare.message().contains("to your hotbar"), "Rare warning replaced immediately");
        ok(immediately.attention() >= 0 && immediately.attention() <= 1, "Alert pulse stays bounded");
        for (var s : List.of(common, uncommon, rare, removedRare)) {
            FakeCanvas canvas = new FakeCanvas();
            CrestHudPainter.draw(canvas, s, animator.sample(s, now += 1000), 500, 300, 85, 12, 1.0);
            ok(canvas.roundCount >= 6, "Rounded card and bar should be visible");
            ok(canvas.glowCount >= 3, "Use Trevor's original soft glows");
            ok(canvas.ellipseCount >= 5, "Use Trevor's paw motif");
            ok(canvas.contains("Trevor's crest"), "Show consistent branding");
            switch (s.kind()) {
                case COMMON -> ok(canvas.contains("42 / 100"), "Show Common live pelt count");
                case ALMOST_THERE -> { ok(canvas.contains("Almost there!"), "Uncommon motivation"); ok(canvas.contains("258 / 500"), "Uncommon counts"); }
                case SWITCH_CREST -> { ok(canvas.contains("500 PELTS REACHED"), "500 pelt completion message"); ok(canvas.contains("SWITCH CRESTS!"), "Bold rotation message"); ok(canvas.contains("Remove crest from slot 3"), "Action names active slot"); }
                case MISSING -> { ok(canvas.contains("MOVE TRAPPER CREST"), "Bold missing crest headline"); ok(canvas.contains("TO YOUR HOTBAR!"), "Missing crest action"); ok(!canvas.contains("SWITCH CRESTS!"), "Switch warning must vanish"); }
            }
        }
        for (int w : new int[]{132, 180, 320, 1440}) {
            FakeCanvas canvas = new FakeCanvas();
            CrestHudPainter.draw(canvas, uncommon, animator.sample(uncommon, now += 1000), w, 180, 85, 12, 1.6);
            ok(canvas.contains("Almost there!"), "Crest widget scales to fit available space at width " + w);
        }
        System.out.println("PASS: " + checks + " crest visual/layout and animation assertions");
    }
}
