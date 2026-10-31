package dev.trevorroll;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;

import java.util.Arrays;

/** Exercises the real registered mapping, tick handler, native Controls and options file. */
public final class MenuKeybindChecks {
    private MenuKeybindChecks() {}

    public static void run(ClientGameTestContext ctx) {
        String[] original = {""};
        Screen[] opened = {null};
        boolean[] menuAnimations = {true};
        boolean[] advancedGraphics = {false};
        ctx.runOnClient(mc -> {
            var key = MenuKeybind.keyMapping();
            original[0] = key.saveString();
            menuAnimations[0] = TrevorRollClient.settings().menuAnimations;
            advancedGraphics[0] = TrevorRollClient.settings().advancedMenuGraphics;
            require(new RollConfig().menuAnimations, "Opening fade defaults on");
            require(!new RollConfig().advancedMenuGraphics, "Advanced menu graphics default to lightweight off");
            TrevorRollClient.settings().menuAnimations = false;
            TrevorRollClient.settings().advancedMenuGraphics = true;
            TrevorRollClient.settings().save();
            require(!RollConfig.load().menuAnimations, "Reduced-motion menu preference persists");
            require(RollConfig.load().advancedMenuGraphics, "Advanced graphics preference persists");
            TrevorRollClient.settings().menuAnimations = menuAnimations[0];
            TrevorRollClient.settings().advancedMenuGraphics = advancedGraphics[0];
            TrevorRollClient.settings().save();
            require(key.getDefaultKey().equals(InputConstants.UNKNOWN), "Menu binding defaults to unbound");
            require(Arrays.asList(mc.options.keyMappings).contains(key), "Menu binding appears in native Controls");
            MenuKeybind.clear();
            require(key.isUnbound() && "Unbound".equals(MenuKeybind.label()), "Reset is visibly unbound");
            mc.gui.setScreen(null);
            KeyMapping.click(InputConstants.UNKNOWN);
        });
        try {
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() == null, "Unbound mapping never opens the menu");
                MenuKeybind.bind(key(InputConstants.KEY_F8));
                require(MenuKeybind.keyMapping().matches(key(InputConstants.KEY_F8)), "Menu capture binds F8");
                require(!MenuKeybind.conflicts(), "Unused key has no conflict");
                // Reload the actual options file rather than only inspecting the in-memory binding.
                MenuKeybind.keyMapping().setKey(InputConstants.UNKNOWN);
                mc.options.load();
                require(MenuKeybind.keyMapping().matches(key(InputConstants.KEY_F8)), "Key binding survives disk reload");
                press(InputConstants.KEY_F8); press(InputConstants.KEY_F8);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() instanceof TrevorSettingsScreen, "Bound key opens real settings on client tick");
                opened[0] = mc.gui.screen();
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() == opened[0], "Queued repeats do not rebuild the menu");
                mc.gui.setScreen(null);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() == null, "Dismissal does not replay queued opening presses");
                mc.gui.setScreen(new ChatScreen("typing a hunt message", false));
                press(InputConstants.KEY_F8);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() instanceof ChatScreen, "Menu shortcut never steals chat input");
                mc.gui.setScreen(null);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() == null, "Chat-discarded shortcut does not open after closing chat");
                var nativeControls = new KeyBindsScreen(null, mc.options);
                mc.gui.setScreen(nativeControls);
                nativeControls.selectedKey = MenuKeybind.keyMapping();
                require(nativeControls.keyPressed(key(InputConstants.KEY_F7)), "Native Controls can rebind the menu key");
                nativeControls.onClose();
                mc.options.save();
                require(MenuKeybind.keyMapping().matches(key(InputConstants.KEY_F7)), "Native Controls and menu share one binding");
                press(InputConstants.KEY_F8);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() == null, "Old shortcut no longer opens settings after rebind");
                press(InputConstants.KEY_F7);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> {
                require(mc.gui.screen() instanceof TrevorSettingsScreen, "Rebound shortcut opens settings");
                MenuKeybind.clear();
                require(MenuKeybind.keyMapping().isDefault(), "Clear restores the unbound default");
                // Prove reset is also persisted, not just the assigned key.
                MenuKeybind.keyMapping().setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F8));
                mc.options.load();
                require(MenuKeybind.keyMapping().isUnbound(), "Unbound reset survives disk reload");
                mc.gui.setScreen(null);
                press(InputConstants.KEY_F7);
            });
            ctx.waitTicks(2);
            ctx.runOnClient(mc -> require(mc.gui.screen() == null, "Cleared shortcut no longer opens settings"));
            System.out.println("PASS: unbound menu key, registration, bind/rebind/reset persistence, native Controls, chat safety and repeat draining");
        } finally {
            ctx.runOnClient(mc -> {
                MenuKeybind.keyMapping().setKey(InputConstants.getKey(original[0]));
                MenuKeybind.keyMapping().setDown(false);
                while (MenuKeybind.keyMapping().consumeClick()) { }
                KeyMapping.resetMapping();
                mc.options.save();
                TrevorRollClient.settings().menuAnimations = menuAnimations[0];
                TrevorRollClient.settings().advancedMenuGraphics = advancedGraphics[0];
                TrevorRollClient.settings().save();
                mc.gui.setScreen(null);
            });
        }
    }

    private static KeyEvent key(int code) { return new KeyEvent(code, 0, 0); }
    private static void press(int code) { KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(code)); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
