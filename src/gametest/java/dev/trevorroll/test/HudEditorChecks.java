package dev.trevorroll;

import dev.crestwatch.trapper.CrestHudConfig;
import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Real Minecraft input, canvas coordinate conversion, corner resize and disk round trips. */
public final class HudEditorChecks {
    private HudEditorChecks() {}
    private static double zoom, ox;
    /** Real wheel input must expose every control, including stacked narrow-screen rows. */
    public static void settingsPage(Minecraft mc, int page) {
        var screen = (TrevorSettingsScreen) mc.gui.screen();
        screen.selectModule(page);
        var body = bodyControls(screen);
        String[][] expected = {
            {"Roll", "Trapper Crest", "Appearance", "HUD Layout", "Jackpots", "Audio Mixer", "Pelt Tracker"},
            {"Roll:", "Sound:", "Roll duration:", "Result stays visible:", "Sound volume:", "Chat result:", "Preview roll"},
            {"Crest HUD:", "Hypixel only:", "Missing reminder:", "Yield to roll:", "Animations:", "Crest size:", "Preview all crest states"},
            {"Roll size:", "Crest size:", "Roll opacity:", "Crest opacity:", "Roll glow:", "Crest glow:"},
            {"Grid:", "Snapping:", "Grid spacing:", "Lock roll:", "Lock crest:", "Open layout editor", "Reset sizes and positions"},
            {"Elusive reward:", "Reward sound:", "Style:", "Reward duration:", "Effect intensity:", "Reward volume:", "Preview reward"},
            {"Layer 1:", "Layer 2:", "Layer 1 volume:", "Layer 2 volume:", "Preview both sounds"},
            {"Show stats:", "Crest alert:", "Start / reset pelt session", "Stop tracker", "Maxed sound 1:", "Maxed sound 2:", "Preview maxed-crest sounds"}
        };
        require(body.size() == expected[page + 1].length, "All existing controls are preserved on module " + page);
        for (String label : expected[page + 1]) require(body.stream().anyMatch(w -> w.getMessage().getString().startsWith(label)),
            "Preserved setting: " + label);
        Set<AbstractWidget> reached = Collections.newSetFromMap(new IdentityHashMap<>());
        int previous = -1;
        for (int step = 0; step < 1000; step++) {
            checkSettingsGeometry(mc, screen);
            for (var widget : body) {
                if (widget.visible && widget.getY() >= screen.viewportTop()
                    && widget.getBottom() <= screen.viewportBottom()) reached.add(widget);
            }
            int scroll = scroll(screen);
            if (scroll == previous) break;
            previous = scroll;
            screen.mouseScrolled(screen.width / 2., (screen.viewportTop() + screen.viewportBottom()) / 2., 0, -.02);
        }
        require(reached.containsAll(body), "Every control becomes fully reachable on module " + page
            + " at " + screen.width + "x" + screen.height + ": "
            + body.stream().filter(w -> !reached.contains(w)).map(w -> w.getMessage().getString()).toList());
        // Neither a clipped slider nor a card may steal input from the surrounding chrome.
        require(!screen.mouseClicked(event(screen.width / 2., screen.viewportTop() - 1), false),
            "Header gap does not activate a clipped setting");
        require(!screen.mouseClicked(event(screen.width / 2., screen.viewportBottom() + 1), false),
            "Footer gap does not activate a clipped setting");
        screen.mouseScrolled(screen.width / 2., (screen.viewportTop() + screen.viewportBottom()) / 2., 0, 10000);
        require(scroll(screen) == 0, "Wheel returns to the first row");
        settingsKeyboard(screen);
    }

    private static void settingsKeyboard(TrevorSettingsScreen screen) {
        var body = bodyControls(screen);
        Set<AbstractWidget> focused = Collections.newSetFromMap(new IdentityHashMap<>());
        int count = widgets(screen).size();
        for (int i = 0; i <= count; i++) {
            require(screen.keyPressed(new KeyEvent(258, 0, 0)), "Tab advances focus");
            if (screen.getFocused() instanceof AbstractWidget widget) {
                require(widget.visible, "Keyboard focus never disappears offscreen");
                if (body.contains(widget)) {
                    require(widget.getY() >= screen.viewportTop() && widget.getBottom() <= screen.viewportBottom(),
                        "Tab fully reveals the focused setting");
                    focused.add(widget);
                }
            }
        }
        require(focused.containsAll(body), "Tab reaches every module control");
        var before = screen.getFocused();
        screen.keyPressed(new KeyEvent(258, 0, 1));
        require(screen.getFocused() != before, "Shift+Tab reverses focus");
        screen.keyPressed(new KeyEvent(269, 0, 0)); // End
        int end = scroll(screen);
        screen.keyPressed(new KeyEvent(268, 0, 0)); // Home
        require(scroll(screen) == 0, "Home returns to the top");
        screen.keyPressed(new KeyEvent(267, 0, 0)); // Page Down
        require(end == 0 || scroll(screen) > 0, "Page Down reveals more settings");
        int down = scroll(screen);
        screen.keyPressed(new KeyEvent(266, 0, 0)); // Page Up
        require(down == 0 || scroll(screen) < down, "Page Up returns toward the top");
        screen.keyPressed(new KeyEvent(268, 0, 0));
    }

    public static void settingsBottom(Minecraft mc) {
        var screen = (TrevorSettingsScreen) mc.gui.screen();
        screen.mouseScrolled(screen.width / 2., (screen.viewportTop() + screen.viewportBottom()) / 2., 0, -10000);
        checkSettingsGeometry(mc, screen);
    }

    public static void settingsInputs(Minecraft mc) {
        var screen = (TrevorSettingsScreen) mc.gui.screen();
        var r = TrevorRollClient.settings();
        boolean enabled = r.enabled;
        int duration = r.durationMillis;
        RewardSound sound = r.elusiveSoundStyle;
        try {
            screen.selectModule(-1);
            click(screen, reveal(screen, "Roll"));
            require(find(screen, "Roll duration:") != null, "Dashboard card opens the Roll module");
            var toggle = reveal(screen, "Roll:");
            click(screen, toggle);
            require(r.enabled != enabled, "Actual toggle input changes the roll setting");
            click(screen, toggle);
            require(r.enabled == enabled, "Repeated toggle restores the roll setting");
            var slider = reveal(screen, "Roll duration:");
            double x = slider.getX() + slider.getWidth() * .8, y = slider.getY() + slider.getHeight() / 2.;
            require(screen.mouseClicked(event(x, y), false), "Actual slider accepts pointer input");
            screen.mouseReleased(event(x, y));
            require(r.durationMillis >= 6000 && r.durationMillis <= 7000, "Actual slider changes duration");
            TrevorSettingsScreen.save();
            require(RollConfig.load().durationMillis == r.durationMillis, "Slider value persists to disk");

            var rail = sidebar(screen);
            if (!rail.isEmpty()) {
                require(rail.size() == 7, "Wide layouts expose all seven sidebar modules");
                click(screen, rail.stream().filter(w -> w.getMessage().getString().equals("Trapper Crest")).findFirst().orElseThrow());
                require(bodyControls(screen).stream().anyMatch(w -> w.getMessage().getString().startsWith("Crest HUD:")),
                    "Sidebar opens the Trapper Crest module directly");
                var rollNavigation = sidebar(screen).stream().filter(w -> w.getMessage().getString().equals("Roll")).findFirst().orElseThrow();
                click(screen, rollNavigation);
                require(bodyControls(screen).stream().anyMatch(w -> w.getMessage().getString().startsWith("Roll duration:")),
                    "Sidebar switches back without returning to the dashboard");
            }
            screen.selectModule(5);
            click(screen, reveal(screen, "Layer 1:"));
            require(mc.gui.screen() instanceof SoundPickerScreen, "Audio layer opens the sound picker");
            var picker = mc.gui.screen();
            RewardSound replacement = sound == RewardSound.BELL ? RewardSound.CHIME : RewardSound.BELL;
            click(picker, find(picker, replacement.label));
            click(picker, find(picker, "Back"));
            require(mc.gui.screen() == screen && r.elusiveSoundStyle == sound, "Back discards the unconfirmed sound");
            click(screen, reveal(screen, "Layer 1:"));
            picker = mc.gui.screen();
            click(picker, find(picker, replacement.label));
            click(picker, find(picker, "Use sound"));
            require(mc.gui.screen() == screen && r.elusiveSoundStyle == replacement, "Use sound applies and returns to the same module");
            TrevorSettingsScreen.save();
            require(RollConfig.load().elusiveSoundStyle == replacement, "Audio choice persists to disk");
            click(screen, find(screen, "Modules"));
            require(find(screen, "Trapper Crest") != null, "Modules returns to the dashboard");
            checkSettingsGeometry(mc, screen);
        } finally {
            r.enabled = enabled; r.durationMillis = duration; r.elusiveSoundStyle = sound;
            TrevorSettingsScreen.save();
            mc.gui.setScreen(screen);
            screen.selectModule(-1);
        }
    }

    private static void checkSettingsGeometry(Minecraft mc, TrevorSettingsScreen screen) {
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        var body = bodyControls(screen);
        int bodyLeft = body.stream().mapToInt(AbstractWidget::getX).min().orElse(screen.width);
        var visible = widgets(screen).stream().filter(widget -> widget.visible).toList();
        for (var widget : visible) {
            require(widget.getX() >= 0 && widget.getRight() <= w, "Control fits horizontal bounds: " + widget.getMessage());
            if (body.contains(widget)) {
                require(widget.getBottom() > screen.viewportTop() && widget.getY() < screen.viewportBottom(),
                    "Visible body control intersects the scissor viewport");
            } else {
                require(widget.getY() >= 0 && widget.getBottom() <= h, "Fixed control fits the screen");
                require(widget.getBottom() <= screen.viewportTop() || widget.getY() >= screen.viewportBottom()
                    || widget.getRight() < bodyLeft,
                    "Fixed header, footer and sidebar stay outside the scrolling content");
            }
        }
        for (int i = 0; i < visible.size(); i++) for (int j = i + 1; j < visible.size(); j++) {
            var a = visible.get(i); var b = visible.get(j);
            int ay = body.contains(a) ? Math.max(a.getY(), screen.viewportTop()) : a.getY();
            int ab = body.contains(a) ? Math.min(a.getBottom(), screen.viewportBottom()) : a.getBottom();
            int by = body.contains(b) ? Math.max(b.getY(), screen.viewportTop()) : b.getY();
            int bb = body.contains(b) ? Math.min(b.getBottom(), screen.viewportBottom()) : b.getBottom();
            require(a.getRight() <= b.getX() || b.getRight() <= a.getX() || ab <= by || bb <= ay,
                "Controls do not overlap: " + a.getMessage() + " / " + b.getMessage());
        }
    }

    private static AbstractWidget reveal(TrevorSettingsScreen screen, String prefix) {
        screen.mouseScrolled(screen.width / 2., (screen.viewportTop() + screen.viewportBottom()) / 2., 0, 10000);
        var widget = bodyControls(screen).stream().filter(w -> w.getMessage().getString().startsWith(prefix)).findFirst().orElse(null);
        require(widget != null, "Scrolling control exists: " + prefix);
        for (int i = 0; i < 1000; i++) {
            if (widget.visible && widget.getY() >= screen.viewportTop() && widget.getBottom() <= screen.viewportBottom()) return widget;
            int before = scroll(screen);
            screen.mouseScrolled(screen.width / 2., (screen.viewportTop() + screen.viewportBottom()) / 2., 0, -.02);
            if (before == scroll(screen)) break;
        }
        throw new AssertionError("Control cannot be fully exposed: " + prefix);
    }
    private static List<AbstractWidget> sidebar(TrevorSettingsScreen screen) {
        var body = bodyControls(screen);
        int bodyLeft = body.stream().mapToInt(AbstractWidget::getX).min().orElse(screen.width);
        return widgets(screen).stream().filter(w -> !body.contains(w) && w.visible && w.getRight() < bodyLeft
            && w.getY() >= screen.viewportTop() && w.getBottom() <= screen.viewportBottom()).toList();
    }
    private static List<AbstractWidget> widgets(Screen screen) {
        return screen.children().stream().filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast).toList();
    }
    private static AbstractWidget find(Screen screen, String prefix) {
        return widgets(screen).stream().filter(w -> w.getMessage().getString().startsWith(prefix)).findFirst().orElse(null);
    }
    @SuppressWarnings("unchecked")
    private static Set<AbstractWidget> bodyControls(TrevorSettingsScreen screen) {
        try {
            var field = TrevorSettingsScreen.class.getDeclaredField("baseY"); field.setAccessible(true);
            return ((Map<AbstractWidget, Integer>) field.get(screen)).keySet();
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static int scroll(TrevorSettingsScreen screen) {
        try {
            var field = TrevorSettingsScreen.class.getDeclaredField("scroll"); field.setAccessible(true);
            return field.getInt(screen);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static void click(Screen screen, AbstractWidget widget) {
        require(widget != null && widget.visible && widget.active, "Clickable control is present");
        double x = widget.getX() + widget.getWidth() / 2., y = widget.getY() + widget.getHeight() / 2.;
        require(screen.mouseClicked(event(x, y), false), "Pointer click handled: " + widget.getMessage());
        screen.mouseReleased(event(x, y));
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
