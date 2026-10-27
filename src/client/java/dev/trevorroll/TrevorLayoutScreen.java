package dev.trevorroll;

import dev.crestwatch.trapper.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.Random;

/** Drag either preview; resize selected panel using the accessible +/- buttons. */
public final class TrevorLayoutScreen extends Screen {
    private final Screen parent;
    private final CrestHudAnimator motion = new CrestHudAnimator();
    private final CrestHudState state = new CrestHudState(CrestHudState.Kind.ALMOST_THERE, 258, 500, 4);
    private Reel preview;
    private final java.util.List<Button> controls = new java.util.ArrayList<>();
    private boolean crestSelected, dragging;
    private double grabX, grabY;

    public TrevorLayoutScreen(Screen parent) {
        super(Component.literal("HUD layout")); this.parent = parent;
    }
    private static long now() { return System.nanoTime() / 1_000_000; }
    @Override protected void init() {
        dragging = false;
        controls.clear();
        int x = width / 2 - 150;
        addControl(Button.builder(Component.literal("Smaller"), b -> resize(-.05)).bounds(x, height - 25, 70, 20).build());
        addControl(Button.builder(Component.literal("Larger"), b -> resize(.05)).bounds(x + 75, height - 25, 70, 20).build());
        addControl(Button.builder(Component.literal("Reset layout"), b -> {
            var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
            r.scale = 1; r.customPosition = false; r.x = .5; r.y = 0;
            c.scale = 1; c.top = 85; c.rightMargin = 12;
        }).bounds(x + 150, height - 25, 80, 20).build());
        addControl(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x + 235, height - 25, 65, 20).build());
    }
    private void addControl(Button button) { controls.add(button); addRenderableWidget(button); }
    private HudLayout.Bounds rollBounds() {
        var r = TrevorRollClient.settings();
        return HudLayout.roll(width, height, r.scale, r.x, r.y, r.customPosition);
    }
    private HudLayout.Bounds crestBounds() {
        var c = TrapperCrestHudClient.settings();
        return HudLayout.crest(width, height, c.scale, c.top, c.rightMargin);
    }
    private void resize(double amount) {
        if (crestSelected) {
            var c = TrapperCrestHudClient.settings(); c.scale = HudLayout.finiteClamp(c.scale + amount, .65, 1.6, 1);
        } else {
            var r = TrevorRollClient.settings(); r.scale = HudLayout.finiteClamp(r.scale + amount, .5, 2, 1);
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != 0) return false;
        var c = crestBounds(); var r = rollBounds();
        if (c.contains(event.x(), event.y())) crestSelected = true;
        else if (r.contains(event.x(), event.y())) crestSelected = false;
        else return false;
        var bounds = crestSelected ? c : r;
        grabX = event.x() - bounds.x(); grabY = event.y() - bounds.y();
        dragging = true; return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!dragging) return super.mouseDragged(event, dx, dy);
        double x = event.x() - grabX, y = event.y() - grabY;
        if (crestSelected) {
            var c = TrapperCrestHudClient.settings(); var b = crestBounds();
            c.top = (int) Math.round(HudLayout.finiteClamp(y, 5, Math.max(5, height - b.height() - 7), 5));
            c.rightMargin = (int) Math.round(HudLayout.finiteClamp(width - b.width() - x, 0, Math.max(0, width - b.width() - 5), 0));
        } else {
            var r = TrevorRollClient.settings(); var b = rollBounds();
            r.customPosition = true;
            r.x = HudLayout.finiteClamp((x - 12) / Math.max(1, width - b.width() - 24), 0, 1, .5);
            r.y = HudLayout.finiteClamp((y - 12) / Math.max(1, height - b.height() - 24), 0, 1, 0);
        }
        return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging && event.button() == 0) { dragging = false; TrevorSettingsScreen.save(); return true; }
        return super.mouseReleased(event);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Keep editor previews fully visible even outside Hypixel or with either HUD disabled.
        long time = now(); var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        if (preview == null || preview.durationMillis != r.durationMillis || preview.expired(time))
            preview = new Reel(new Hunt(Hunt.Rarity.ELUSIVE, "Overgrown Mushroom Cave"), time - 250, r.durationMillis, true, new Random(42));
        // Render screen background first, previews next, controls last.
        g.fill(0, 0, width, height, 0xB018182C);
        HudPainter.draw(new MinecraftCanvas(g), preview, time, width, height, r.scale, r.x, r.y, r.customPosition);
        var bounds = crestBounds();
        CrestHudPainter.draw(new CrestHudCanvas(g), state, motion.sample(state, time), width, height, c.top, c.rightMargin, c.scale);
        outline(g, crestSelected ? bounds : rollBounds());
        // Render widgets directly so Screen's background does not obscure the previews.
        for (var widget : controls) widget.extractRenderState(g, mouseX, mouseY, delta);
        String hint = "Drag a panel. Selected: " + (crestSelected ? "Crest" : "Roll") + " | Esc saves";
        g.text(font, hint, Math.max(4, (width - font.width(hint)) / 2), 5, 0xFFF6F2FF, true);
    }
    private static void outline(GuiGraphicsExtractor g, HudLayout.Bounds b) {
        int x = (int) b.x(), y = (int) b.y(), right = (int) (b.x() + b.width()), bottom = (int) (b.y() + b.height());
        g.fill(x, y, right, y + 1, 0xFFD8C7FF); g.fill(x, bottom - 1, right, bottom, 0xFFD8C7FF);
        g.fill(x, y, x + 1, bottom, 0xFFD8C7FF); g.fill(right - 1, y, right, bottom, 0xFFD8C7FF);
    }
    @Override public void removed() { TrevorSettingsScreen.save(); }
    @Override public void onClose() { TrevorSettingsScreen.save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
