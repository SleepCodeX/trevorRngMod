package dev.trevorroll;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import dev.crestwatch.trapper.CrestHudConfig;
import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Real Minecraft input, canvas coordinate conversion, corner resize and disk round trips. */
public final class HudEditorChecks {
    private HudEditorChecks() {}
    private static double zoom, ox;
    /** Real wheel input must expose every control, including stacked narrow-screen rows. */
    public static void settingsPage(Minecraft mc, int page) {
        var screen = (TrevorSettingsScreen) mc.gui.screen();
        screen.setSearch(""); screen.selectFilter(0); screen.selectModule(page);
        var body = bodyControls(screen);
        String[][] expected = {
            {"Roll", "Trapper Crest", "Appearance", "HUD Layout", "Jackpots", "Audio Mixer", "Pelt Tracker", "Menu & Keys"},
            {"Roll:", "Sound:", "Roll duration:", "Result stays visible:", "Sound volume:", "Chat result:", "Preview roll"},
            {"Crest HUD:", "Hypixel only:", "Missing reminder:", "Yield to roll:", "Animations:", "Crest size:", "Preview all crest states"},
            {"Roll size:", "Crest size:", "Roll opacity:", "Crest opacity:", "Roll glow:", "Crest glow:"},
            {"Grid:", "Snapping:", "Grid spacing:", "Lock roll:", "Lock crest:", "Open layout editor", "Reset sizes and positions"},
            {"Elusive reward:", "Reward sound:", "Style:", "Reward duration:", "Effect intensity:", "Reward volume:", "Preview reward"},
            {"Layer 1:", "Layer 2:", "Layer 1 volume:", "Layer 2 volume:", "Preview both sounds"},
            {"Show stats:", "Crest alert:", "Start / reset pelt session", "Stop tracker", "Maxed sound 1:", "Maxed sound 2:", "Preview maxed-crest sounds"},
            {"Open menu key:", "Reset key to unbound", "Menu fade:", "Advanced graphics:"}
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
        checkMaskedInput(screen, screen.viewportTop() - 1);
        checkMaskedInput(screen, screen.viewportBottom() + 1);
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
        screen.setFocused(null); // Page keys should scroll, not move a search-field caret.
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

    public static boolean setAdvancedPreview(boolean advanced) {
        boolean previous = TrevorRollClient.settings().advancedMenuGraphics;
        TrevorRollClient.settings().advancedMenuGraphics = advanced;
        return previous;
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
            double y = slider.getY() + slider.getHeight() / 2.;
            require(screen.mouseClicked(event(slider.getX() + 4, y), false), "Drawn slider start accepts pointer input");
            screen.mouseReleased(event(slider.getX() + 4, y));
            require(r.durationMillis == 1000, "Drawn slider start maps to the exact minimum");
            require(screen.mouseClicked(event(slider.getRight() - 4, y), false), "Drawn slider end accepts pointer input");
            screen.mouseReleased(event(slider.getRight() - 4, y));
            require(r.durationMillis == 8000, "Drawn slider end maps to the exact maximum");
            double x = slider.getX() + slider.getWidth() * .8;
            require(screen.mouseClicked(event(x, y), false), "Actual slider accepts pointer input");
            screen.mouseReleased(event(x, y));
            require(r.durationMillis >= 6000 && r.durationMillis <= 7000, "Actual slider changes duration");
            TrevorSettingsScreen.save();
            require(RollConfig.load().durationMillis == r.durationMillis, "Slider value persists to disk");
            require(screen.getFocused() == slider, "Click focuses the duration slider");
            screen.keyPressed(new KeyEvent(269, 0, 0)); // End scrolls without transferring keyboard input to a hidden row.
            boolean masked = slider.getY() < screen.viewportTop() || slider.getBottom() > screen.viewportBottom();
            if (screen.height <= 240) require(masked, "Compact viewport exercises a scrolled-out slider");
            if (masked) {
                require(screen.getFocused() == null, "Scrolling a focused slider out of view clears its focus");
                int beforeArrow = r.durationMillis;
                screen.keyPressed(new KeyEvent(262, 0, 0));
                require(r.durationMillis == beforeArrow, "Arrow input cannot alter a clipped or hidden slider");
            }

            var rail = sidebar(screen);
            if (!rail.isEmpty()) {
                require(rail.size() == 5, "Wide layouts expose all five utility shortcuts");
                click(screen, rail.stream().filter(w -> w.getMessage().getString().equals("Appearance")).findFirst().orElseThrow());
                require(bodyControls(screen).stream().anyMatch(w -> w.getMessage().getString().startsWith("Roll opacity:")),
                    "Utility sidebar opens Appearance directly");
                click(screen, sidebar(screen).stream().filter(w -> w.getMessage().getString().equals("Menu & Keys")).findFirst().orElseThrow());
                require(bodyControls(screen).stream().anyMatch(w -> w.getMessage().getString().startsWith("Open menu key:")),
                    "Utility sidebar opens Menu & Keys directly");
                click(screen, sidebar(screen).stream().filter(w -> w.getMessage().getString().equals("Modules")).findFirst().orElseThrow());
                require(bodyControls(screen).size() == 8, "Utility sidebar returns to all modules");
            }
            settingsMenuPreferences(mc, screen);
            settingsFilters(mc, screen);
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

    private static void settingsFilters(Minecraft mc, TrevorSettingsScreen screen) {
        screen.selectModule(-1); screen.setSearch(""); screen.selectFilter(0);
        try {
            String[] names = {"All", "Hunts", "Visuals", "Audio"};
            String[][] expected = {
                {"Roll", "Trapper Crest", "Appearance", "HUD Layout", "Jackpots", "Audio Mixer", "Pelt Tracker", "Menu & Keys"},
                {"Roll", "Trapper Crest", "Pelt Tracker"},
                {"Appearance", "HUD Layout", "Jackpots"},
                {"Audio Mixer"}
            };
            for (int i = 0; i < names.length; i++) {
                final String name = names[i];
                click(screen, widgets(screen).stream().filter(w -> w.getMessage().getString().equals(name)).findFirst().orElseThrow());
                requireModuleNames(screen, expected[i]);
                checkSettingsGeometry(mc, screen);
            }
            screen.selectFilter(0);
            // Type through the actual focused EditBox and its responder.
            var search = (EditBox) widgets(screen).stream().filter(EditBox.class::isInstance).findFirst().orElseThrow();
            click(screen, search);
            for (int codepoint : "  cReSt  ".codePoints().toArray())
                require(screen.charTyped(new CharacterEvent(codepoint)), "Native search field accepts typed text");
            require(search.getValue().equals("  cReSt  "), "Typed search is retained in the native field");
            require(screen.keyPressed(new KeyEvent(259, 0, 0)), "Native search supports Backspace");
            requireModuleNames(screen, "Trapper Crest", "Pelt Tracker");
            screen.setSearch("Menu & Keys");
            requireModuleNames(screen, "Menu & Keys");
            click(screen, reveal(screen, "Menu & Keys"));
            require(find(screen, "Open menu key:") != null, "A search result opens its actual settings");
            screen.selectModule(-1);
            screen.selectFilter(1); screen.setSearch("roll");
            requireModuleNames(screen, "Roll");
            screen.setSearch("sound");
            requireModuleNames(screen); // Hunts category must not leak audio matches.
            screen.selectFilter(3);
            requireModuleNames(screen, "Audio Mixer"); // Matches its description, not only its title.
            screen.setSearch("there-is-no-such-trevor-module");
            requireModuleNames(screen);
            checkSettingsGeometry(mc, screen);
            settingsKeyboard(screen); // Hidden search results must not take keyboard focus.
        } finally {
            screen.setSearch(""); screen.selectFilter(0); screen.selectModule(-1);
        }
        requireModuleNames(screen, "Roll", "Trapper Crest", "Appearance", "HUD Layout", "Jackpots", "Audio Mixer", "Pelt Tracker", "Menu & Keys");
    }

    private static void requireModuleNames(TrevorSettingsScreen screen, String... names) {
        var actual = bodyControls(screen).stream().map(w -> w.getMessage().getString()).collect(java.util.stream.Collectors.toSet());
        require(actual.equals(Set.of(names)), "Filtered module inventory: expected " + List.of(names) + ", found " + actual);
    }

    private static void settingsMenuPreferences(Minecraft mc, TrevorSettingsScreen screen) {
        var r = TrevorRollClient.settings();
        boolean fade = r.menuAnimations, advanced = r.advancedMenuGraphics;
        String originalKey = MenuKeybind.keyMapping().saveString();
        try {
            screen.selectModule(7);
            click(screen, reveal(screen, "Open menu key:"));
            require(find(screen, "Press a key") != null, "Click enters inline shortcut capture");
            require(screen.keyPressed(new KeyEvent(256, 0, 0)), "Escape cancels key capture");
            require(mc.gui.screen() == screen && MenuKeybind.keyMapping().saveString().equals(originalKey),
                "Capture cancellation keeps the menu and prior binding");
            click(screen, reveal(screen, "Open menu key:"));
            screen.keyPressed(new KeyEvent(InputConstants.KEY_F8, 0, 0));
            require(MenuKeybind.keyMapping().matches(new KeyEvent(InputConstants.KEY_F8, 0, 0)), "Inline capture assigns F8");
            require(find(screen, "Open menu key:").getMessage().getString().contains(MenuKeybind.label()),
                "Shortcut row shows the newly assigned key");
            MenuKeybind.keyMapping().setKey(InputConstants.UNKNOWN);
            mc.options.load();
            require(MenuKeybind.keyMapping().matches(new KeyEvent(InputConstants.KEY_F8, 0, 0)), "Inline binding survives options reload");
            click(screen, reveal(screen, "Open menu key:"));
            screen.keyPressed(new KeyEvent(261, 0, 0));
            require(MenuKeybind.keyMapping().isUnbound(), "Delete clears the captured shortcut");
            click(screen, reveal(screen, "Open menu key:"));
            screen.keyPressed(new KeyEvent(InputConstants.KEY_F8, 0, 0));
            click(screen, reveal(screen, "Reset key to unbound"));
            require(MenuKeybind.keyMapping().isUnbound(), "Reset key button clears the shortcut");
            click(screen, reveal(screen, "Menu fade:"));
            click(screen, reveal(screen, "Advanced graphics:"));
            require(r.menuAnimations != fade && r.advancedMenuGraphics != advanced, "Actual graphics toggles change preferences");
            TrevorSettingsScreen.save();
            var saved = RollConfig.load();
            require(saved.menuAnimations == r.menuAnimations && saved.advancedMenuGraphics == r.advancedMenuGraphics,
                "Menu motion and advanced graphics preferences persist");
        } finally {
            r.menuAnimations = fade; r.advancedMenuGraphics = advanced; TrevorSettingsScreen.save();
            MenuKeybind.keyMapping().setKey(InputConstants.getKey(originalKey));
            MenuKeybind.keyMapping().setDown(false);
            while (MenuKeybind.keyMapping().consumeClick()) { }
            KeyMapping.resetMapping(); mc.options.save();
        }
    }

    private static void checkMaskedInput(TrevorSettingsScreen screen, int y) {
        var body = bodyControls(screen);
        int left = body.stream().mapToInt(AbstractWidget::getX).min().orElse(0);
        int right = body.stream().mapToInt(AbstractWidget::getRight).max().orElse(0);
        for (int x = left; x < right; x++) {
            final int at = x;
            // A toolbar/search control is intentionally clickable; only test uncovered chrome.
            boolean fixedHit = widgets(screen).stream().anyMatch(w -> !body.contains(w) && w.visible
                && at >= w.getX() && at < w.getRight() && y >= w.getY() && y < w.getBottom());
            if (!fixedHit) {
                require(!screen.mouseClicked(event(x, y), false), "Masked content does not steal input from surrounding chrome");
                return;
            }
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
    @SuppressWarnings("unchecked")
    private static List<AbstractWidget> sidebar(TrevorSettingsScreen screen) {
        try {
            var field = TrevorSettingsScreen.class.getDeclaredField("navigation"); field.setAccessible(true);
            return List.copyOf(((Map<AbstractWidget, Integer>) field.get(screen)).keySet());
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
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
