package dev.trevorroll;

import dev.crestwatch.trapper.CrestHudConfig;
import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Real Minecraft input, canvas coordinate conversion, corner resize and disk round trips. */
public final class HudEditorChecks {
    private HudEditorChecks() {}
    private static double zoom, ox;
    public static void settingsPage(Minecraft mc, int page) {
        var screen = (TrevorSettingsScreen) mc.gui.screen();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        int content = Math.min(440, w - 28), left = (w - content) / 2;
        var e = event(left + page * (content / 6) + 10, 48);
        screen.mouseClicked(e, false); screen.mouseReleased(e);
        for (var child : screen.children()) if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
            require(widget.getX() >= 0 && widget.getY() >= 0 && widget.getRight() <= w && widget.getBottom() <= h,
                "Settings control is reachable on page " + page);
        }
    }
    public static void run(Minecraft mc) {
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        var editor = (TrevorLayoutScreen) mc.gui.screen();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        zoom = Math.max(.1, Math.min((w - 28.) / w, (h - 132.) / h)); ox = (w - w * zoom) / 2;
        boolean snap = r.editorSnap; r.editorSnap = false;
        var b = HudLayout.roll(w, h, r.scale, r.x, r.y, r.customPosition);
        // Click inside the card but outside the corner handle hit area.
        require(editor.mouseClicked(point(b.x() + b.width() / 3, b.y() + b.height() / 3), false), "Select roll");
        require(editor.mouseDragged(point(b.x() + b.width() / 3, b.y() + b.height() / 3 + 35), 0, 35), "Drag roll");
        editor.mouseReleased(point(50, 80));
        require(r.customPosition && r.y > 0, "Drag updates roll position");
        require(RollConfig.load().y == r.y, "Roll drag persists on disk");
        int cw = (w - 28) / 4;
        click(editor, 14 + cw + 10, 20); // Explicitly select the crest even in an overlap.
        b = HudLayout.crest(w, h, c.scale, c.top, c.rightMargin);
        require(editor.mouseClicked(point(b.x() + 40, b.y() + 40), false), "Select crest");
        require(editor.mouseDragged(point(60, 70), 10, 10), "Drag crest");
        editor.mouseReleased(point(60, 70));
        require(c.top == 30, "Drag updates crest top");
        require(CrestHudConfig.load().top == c.top, "Crest drag persists on disk");
        b = HudLayout.crest(w, h, c.scale, c.top, c.rightMargin);
        double old = b.scale();
        require(editor.mouseClicked(point(b.x() + b.width(), b.y() + b.height()), false), "Grab corner handle");
        require(editor.mouseDragged(point(b.x() + b.width() + 25, b.y() + b.height() + 14), 25, 14), "Resize by corner");
        editor.mouseReleased(point(250, 180));
        require(c.scale > old, "Corner resize increases scale");
        double resized = c.scale;
        click(editor, 14 + 10, 44); // Undo corner resize.
        require(Math.abs(c.scale - old) < .001, "Undo restores size");
        click(editor, 14 + 2 * cw + 10, 44); // Lock.
        b = HudLayout.crest(w, h, c.scale, c.top, c.rightMargin);
        int prior = c.top;
        editor.mouseClicked(point(b.x() + 40, b.y() + 40), false);
        editor.mouseDragged(point(70, 100), 10, 10);
        editor.mouseReleased(point(70, 100));
        require(c.top == prior, "Locked panel cannot move");
        click(editor, 14 + 2 * cw + 10, 44); // Unlock.
        int bw = (w - 30) / 5;
        click(editor, 15 + bw + 10, h - 16);
        require(c.scale > old, "Size button changes selected panel");
        r.opacity = .7; r.volume = .35; r.resultMillis = 6000;
        c.opacity = .6; c.animations = false; c.showMissing = false;
        TrevorSettingsScreen.save();
        var sr = RollConfig.load(); var sc = CrestHudConfig.load();
        require(sr.opacity == .7 && sr.volume == .35 && sr.resultMillis == 6000, "New roll settings persist");
        require(sc.opacity == .6 && !sc.animations && !sc.showMissing, "New crest settings persist");
        r.customPosition = false; r.x = .5; r.y = 0; r.opacity = 1; r.volume = 1; r.resultMillis = 4500; r.editorSnap = snap;
        c.scale = 1; c.top = 85; c.rightMargin = 12; c.opacity = 1; c.animations = true; c.showMissing = true;
        TrevorSettingsScreen.save();
    }
    private static MouseButtonEvent point(double x, double y) { return event(ox + x * zoom, 64 + y * zoom); }
    private static MouseButtonEvent event(double x, double y) { return new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)); }
    private static void click(TrevorLayoutScreen e, double x, double y) { e.mouseClicked(event(x, y), false); e.mouseReleased(event(x, y)); }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
