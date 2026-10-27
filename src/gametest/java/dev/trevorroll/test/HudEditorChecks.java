package dev.trevorroll;

import dev.crestwatch.trapper.CrestHudConfig;
import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Exercises actual screen input and disk round trips against Minecraft classes. */
public final class HudEditorChecks {
    private HudEditorChecks() {}
    public static void run(Minecraft mc) {
        var roll = TrevorRollClient.settings(); var crest = TrapperCrestHudClient.settings();
        var editor = (TrevorLayoutScreen) mc.gui.screen();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        var bounds = HudLayout.roll(w, h, roll.scale, roll.x, roll.y, roll.customPosition);
        require(editor.mouseClicked(event(bounds.x() + 10, bounds.y() + 10), false), "Select roll");
        require(editor.mouseDragged(event(35, 70), 10, 10), "Drag roll");
        require(editor.mouseReleased(event(35, 70)), "Release roll");
        require(roll.customPosition && roll.y > 0, "Drag updates roll position");
        var savedRoll = RollConfig.load();
        require(savedRoll.customPosition && savedRoll.y == roll.y, "Roll drag persists on disk");
        var c = HudLayout.crest(w, h, crest.scale, crest.top, crest.rightMargin);
        require(editor.mouseClicked(event(c.x() + 10, c.y() + 10), false), "Select crest");
        require(editor.mouseDragged(event(30, 40), 10, 10), "Drag crest");
        require(editor.mouseReleased(event(30, 40)), "Release crest");
        require(crest.top == 30, "Drag updates crest top");
        var savedCrest = CrestHudConfig.load();
        require(savedCrest.top == crest.top && savedCrest.rightMargin == crest.rightMargin, "Crest drag persists on disk");
        roll.customPosition = false; roll.x = .5; roll.y = 0;
        crest.top = 85; crest.rightMargin = 12;
        TrevorSettingsScreen.save();
    }
    private static MouseButtonEvent event(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
    }
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
