package dev.trevorroll;

import dev.crestwatch.trapper.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Screen-isolated canvas with stable previews, selection, handles and reversible edits. */
public final class TrevorLayoutScreen extends Screen {
    private final Screen parent;
    private final List<Button> controls = new ArrayList<>();
    private final Deque<Snapshot> undo = new ArrayDeque<>();
    private final Snapshot original = snapshot();
    private Snapshot gesture;
    private boolean crestSelected, dragging, resizing;
    private int corner, previewKind = 2;
    private double grabX, grabY, anchorX, anchorY, guideX = Double.NaN, guideY = Double.NaN;
    private double zoom, viewX, viewY;
    private record Snapshot(double rs, double rx, double ry, boolean custom, double cs, int ct, int cr) {}
    public TrevorLayoutScreen(Screen parent) { super(Component.literal("HUD studio")); this.parent = parent; }
    private static Snapshot snapshot() {
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        return new Snapshot(r.scale, r.x, r.y, r.customPosition, c.scale, c.top, c.rightMargin);
    }
    private static void restore(Snapshot s) {
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        r.scale = s.rs; r.x = s.rx; r.y = s.ry; r.customPosition = s.custom;
        c.scale = s.cs; c.top = s.ct; c.rightMargin = s.cr;
    }
    @Override protected void init() {
        dragging = resizing = false; controls.clear();
        zoom = Math.max(.1, Math.min((width - 28.) / width, (height - 132.) / height));
        viewX = (width - width * zoom) / 2; viewY = 64;
        int cw = (width - 28) / 4;
        add("Roll", 0, 10, cw, () -> { crestSelected = false; rebuildWidgets(); });
        add("Crest", 1, 10, cw, () -> { crestSelected = true; rebuildWidgets(); });
        var r = TrevorRollClient.settings();
        add("Snap: " + on(r.editorSnap), 2, 10, cw, () -> { r.editorSnap = !r.editorSnap; rebuildWidgets(); });
        add("Grid: " + on(r.editorGrid), 3, 10, cw, () -> { r.editorGrid = !r.editorGrid; rebuildWidgets(); });
        add("Undo", 0, 34, cw, this::undo);
        add("Center", 1, 34, cw, () -> { if (!locked()) { remember(); var b = selected(); place((width - b.width()) / 2, (height - b.height()) / 2); } });
        add(locked() ? "Unlock" : "Lock", 2, 34, cw, () -> {
            if (crestSelected) r.crestLocked = !r.crestLocked; else r.rollLocked = !r.rollLocked; rebuildWidgets();
        });
        add("Done", 3, 34, cw, this::onClose);
        int bw = (width - 30) / 5;
        bottom("− Size", 0, bw, () -> changeScale(-.025));
        bottom("+ Size", 1, bw, () -> changeScale(.025));
        bottom("Preview", 2, bw, () -> previewKind = (previewKind + 1) % 4);
        bottom("Reset", 3, bw, () -> {
            if (locked()) return; remember();
            var c = TrapperCrestHudClient.settings();
            if (crestSelected) { c.scale = 1; c.top = 85; c.rightMargin = 12; }
            else { r.scale = 1; r.x = .5; r.y = 0; r.customPosition = false; }
        });
        bottom("Cancel", 4, bw, () -> { restore(original); onClose(); });
    }
    private static String on(boolean b) { return b ? "ON" : "OFF"; }
    private void add(String label, int index, int y, int w, Runnable action) { button(label, 14 + index * w, y, w - 4, action); }
    private void bottom(String label, int i, int w, Runnable action) { button(label, 15 + i * w, height - 26, w - 4, action); }
    private void button(String text, int x, int y, int w, Runnable action) {
        var b = Button.builder(Component.literal(text), ignored -> action.run()).bounds(x, y, w, 20).build();
        controls.add(b); addRenderableWidget(b);
    }
    private boolean locked() { var r = TrevorRollClient.settings(); return crestSelected ? r.crestLocked : r.rollLocked; }
    private HudLayout.Bounds rollBounds() { var r = TrevorRollClient.settings(); return HudLayout.roll(width, height, r.scale, r.x, r.y, r.customPosition); }
    private HudLayout.Bounds crestBounds() { var c = TrapperCrestHudClient.settings(); return HudLayout.crest(width, height, c.scale, c.top, c.rightMargin); }
    private HudLayout.Bounds selected() { return crestSelected ? crestBounds() : rollBounds(); }
    private void remember() { if (undo.size() >= 50) undo.removeLast(); undo.push(snapshot()); }
    private void undo() { if (!undo.isEmpty()) restore(undo.pop()); dragging = resizing = false; }
    private double vx(double x) { return (x - viewX) / zoom; }
    private double vy(double y) { return (y - viewY) / zoom; }
    private void place(double x, double y) {
        var b = selected();
        if (crestSelected) {
            var c = TrapperCrestHudClient.settings();
            c.top = (int) Math.round(HudLayout.finiteClamp(y, 5, Math.max(5, height - b.height() - 7), 5));
            c.rightMargin = (int) Math.round(HudLayout.finiteClamp(width - b.width() - x, 0, Math.max(0, width - b.width() - 5), 0));
        } else {
            var r = TrevorRollClient.settings(); r.customPosition = true;
            r.x = HudLayout.finiteClamp((x - 12) / Math.max(1, width - b.width() - 24), 0, 1, .5);
            r.y = HudLayout.finiteClamp((y - 12) / Math.max(1, height - b.height() - 24), 0, 1, 0);
        }
    }
    private void setScale(double scale) {
        if (crestSelected) TrapperCrestHudClient.settings().scale = HudLayout.finiteClamp(scale, .65, 1.6, 1);
        else TrevorRollClient.settings().scale = HudLayout.finiteClamp(scale, .5, 2, 1);
    }
    private void changeScale(double delta) {
        if (locked()) return;
        remember(); var b = selected(); setScale(b.scale() + delta); place(b.x(), b.y());
    }
    private int hitHandle(double x, double y) {
        var b = selected(); double tolerance = 6 / zoom;
        for (int i = 0; i < 4; i++) {
            double hx = b.x() + (i % 2) * b.width(), hy = b.y() + (i / 2) * b.height();
            if (Math.abs(x - hx) <= tolerance && Math.abs(y - hy) <= tolerance) return i;
        }
        return -1;
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        if (super.mouseClicked(e, twice)) return true;
        if (e.button() != 0 || e.y() < viewY || e.y() > height - 68) return false;
        double x = vx(e.x()), y = vy(e.y());
        corner = hitHandle(x, y);
        if (corner < 0) {
            // The selected card takes priority in overlaps; toolbar buttons always select hidden cards.
            if (!selected().contains(x, y)) {
                if (crestBounds().contains(x, y)) crestSelected = true;
                else if (rollBounds().contains(x, y)) crestSelected = false;
                else return false;
            }
        }
        if (locked()) return true;
        gesture = snapshot(); remember();
        var b = selected(); grabX = x - b.x(); grabY = y - b.y();
        resizing = corner >= 0; dragging = !resizing;
        anchorX = b.x() + (corner % 2 == 0 ? b.width() : 0);
        anchorY = b.y() + (corner / 2 == 0 ? b.height() : 0);
        return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (!dragging && !resizing) return super.mouseDragged(e, dx, dy);
        double x = vx(e.x()), y = vy(e.y()); guideX = guideY = Double.NaN;
        if (resizing) {
            double lw = crestSelected ? 228 : 414, lh = crestSelected ? 118 : 160;
            double sx = corner % 2 == 0 ? -1 : 1, sy = corner / 2 == 0 ? -1 : 1;
            double maxX = sx < 0 ? anchorX - 12 : width - anchorX - 12;
            double maxY = sy < 0 ? anchorY - 12 : height - anchorY - 12;
            double max = Math.min(crestSelected ? 1.6 : 2, Math.min(maxX / lw, maxY / lh));
            double min = crestSelected ? .65 : .5;
            setScale(EditorGeometry.resize(sx * (x - anchorX), sy * (y - anchorY), lw, lh, Math.min(min, max), Math.max(min, max)));
            var b = selected(); place(sx < 0 ? anchorX - b.width() : anchorX, sy < 0 ? anchorY - b.height() : anchorY);
        } else {
            x -= grabX; y -= grabY;
            var r = TrevorRollClient.settings(); var b = selected(); var other = crestSelected ? rollBounds() : crestBounds();
            if (r.editorSnap && (e.modifiers() & 1) == 0) {
                var xs = EditorGeometry.snap(x, b.width(), width, other.x(), other.width(), r.editorGrid ? r.gridSize : 0, 5 / zoom);
                var ys = EditorGeometry.snap(y, b.height(), height, other.y(), other.height(), r.editorGrid ? r.gridSize : 0, 5 / zoom);
                x = xs.position(); y = ys.position(); guideX = xs.guide(); guideY = ys.guide();
            }
            place(x, y);
        }
        return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        if ((dragging || resizing) && e.button() == 0) {
            dragging = resizing = false; guideX = guideY = Double.NaN; TrevorSettingsScreen.save(); return true;
        }
        return super.mouseReleased(e);
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (y >= viewY && y < height - 68 && vertical != 0) { changeScale(Math.copySign(.025, vertical)); return true; }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if (e.key() == 256 && (dragging || resizing)) { restore(gesture); dragging = resizing = false; return true; }
        if (e.key() == 90 && (e.modifiers() & 2) != 0) { undo(); return true; }
        if (!locked() && e.key() >= 262 && e.key() <= 265) {
            remember(); var b = selected(); double step = (e.modifiers() & 1) != 0 ? 10 : 1;
            place(b.x() + (e.key() == 262 ? step : e.key() == 263 ? -step : 0),
                b.y() + (e.key() == 264 ? step : e.key() == 265 ? -step : 0)); return true;
        }
        return super.keyPressed(e);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xFF101222);
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        g.pose().pushMatrix(); g.pose().translate((float) viewX, (float) viewY); g.pose().scale((float) zoom, (float) zoom);
        try {
            g.fill(0, 0, width, height, 0xFF1B1E31);
            if (r.editorGrid) {
                for (int x = 0; x < width; x += r.gridSize) g.fill(x, 0, x + 1, height, 0x183F4568);
                for (int y = 0; y < height; y += r.gridSize) g.fill(0, y, width, y + 1, 0x183F4568);
            }
            long time = System.nanoTime() / 1_000_000;
            var reel = new Reel(new Hunt(Hunt.Rarity.ELUSIVE, "Overgrown Mushroom Cave"), time - r.durationMillis - 500, r.durationMillis, true, new Random(42), r.resultMillis);
            var kinds = CrestHudState.Kind.values();
            var state = new CrestHudState(kinds[previewKind], previewKind == 0 ? -1 : previewKind == 3 ? 504 : previewKind == 1 ? 42 : 258, previewKind == 1 ? 100 : 500, 4);
            if (crestSelected) {
                drawRoll(g, reel, time); drawCrest(g, state);
            } else { drawCrest(g, state); drawRoll(g, reel, time); }
            outline(g, crestSelected ? rollBounds() : crestBounds(), 0xFF555A75, false);
            outline(g, selected(), locked() ? 0xFFFFD28D : 0xFFD0B8FF, true);
            if (Double.isFinite(guideX)) g.fill((int) guideX, 0, (int) guideX + 1, height, 0xFF75E4DC);
            if (Double.isFinite(guideY)) g.fill(0, (int) guideY, width, (int) guideY + 1, 0xFF75E4DC);
        } finally { g.pose().popMatrix(); }
        for (var button : controls) button.extractRenderState(g, mx, my, delta);
        var b = selected();
        String label = String.format(Locale.ROOT, "%s%s  ·  X %.0f  Y %.0f  ·  %.0f × %.0f  ·  %.0f%%", crestSelected ? "CREST" : "ROLL", locked() ? " / LOCKED" : "", b.x(), b.y(), b.width(), b.height(), b.scale() * 100);
        g.text(font, label, 15, height - 61, 0xFFF1E9FF, false);
        g.text(font, "Drag corners to resize • Scroll for size • Arrows to nudge", 15, height - 48, 0xFFA9A5C0, false);
        g.text(font, "Shift: free drag / 10px nudge • Ctrl+Z: undo • Esc: save", 15, height - 37, 0xFF8283A1, false);
    }
    private void drawRoll(GuiGraphicsExtractor g, Reel reel, long time) {
        var r = TrevorRollClient.settings(); HudPainter.draw(new MinecraftCanvas(g, r.opacity, r.glow), reel, time, width, height, r.scale, r.x, r.y, r.customPosition);
    }
    private void drawCrest(GuiGraphicsExtractor g, CrestHudState state) {
        var c = TrapperCrestHudClient.settings(); CrestHudPainter.draw(new CrestHudCanvas(g, c.opacity, c.glow), state,
            new CrestHudFrame(state.ratio(), 0, 0, 1, 0, 0), width, height, c.top, c.rightMargin, c.scale);
    }
    private void outline(GuiGraphicsExtractor g, HudLayout.Bounds b, int color, boolean handles) {
        int x = (int) b.x(), y = (int) b.y(), right = (int) (b.x() + b.width()), bottom = (int) (b.y() + b.height());
        g.fill(x, y, right, y + 1, color); g.fill(x, bottom - 1, right, bottom, color);
        g.fill(x, y, x + 1, bottom, color); g.fill(right - 1, y, right, bottom, color);
        if (handles && !locked()) for (int i = 0; i < 4; i++) {
            int hx = i % 2 == 0 ? x : right, hy = i / 2 == 0 ? y : bottom, s = (int) Math.ceil(3 / zoom);
            g.fill(hx - s - 1, hy - s - 1, hx + s + 1, hy + s + 1, 0xFF17182B);
            g.fill(hx - s, hy - s, hx + s, hy + s, color);
        }
    }
    @Override public void removed() { TrevorSettingsScreen.save(); }
    @Override public void onClose() { TrevorSettingsScreen.save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
