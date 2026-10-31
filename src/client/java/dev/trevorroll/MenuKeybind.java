package dev.trevorroll;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

/** One native, persisted binding shared by Minecraft Controls and the mod menu. */
public final class MenuKeybind {
    private static KeyMapping openMenu;

    private MenuKeybind() {}

    static void register() {
        if (openMenu != null) return;
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("trevorroll", "menu"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.trevorroll.open_menu", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));
    }

    /** Drain queued repeats even while another screen is open; never steal chat input. */
    static void tick(Minecraft client) {
        boolean pressed = false;
        while (openMenu.consumeClick()) pressed = true;
        if (pressed && !openMenu.isUnbound() && client.gui.screen() == null
            && client.player != null && client.level != null) {
            client.gui.setScreen(new TrevorSettingsScreen());
        }
    }

    public static String label() {
        return openMenu.isUnbound() ? "Unbound" : openMenu.getTranslatedKeyMessage().getString();
    }

    public static void bind(KeyEvent event) {
        assign(InputConstants.getKey(event));
    }

    public static void clear() {
        assign(InputConstants.UNKNOWN);
    }

    private static void assign(InputConstants.Key key) {
        openMenu.setKey(key);
        openMenu.setDown(false);
        while (openMenu.consumeClick()) { /* Do not replay a pre-rebind press. */ }
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    /** Native Controls remains the source of truth, including bindings made to mouse buttons. */
    static KeyMapping keyMapping() {
        return openMenu;
    }

    public static boolean conflicts() {
        if (openMenu.isUnbound()) return false;
        for (var other : Minecraft.getInstance().options.keyMappings) {
            if (other != openMenu && !other.isUnbound() && openMenu.same(other)) return true;
        }
        return false;
    }
}
